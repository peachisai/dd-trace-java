package com.datadog.debugger.symbol;

import static com.datadog.debugger.symbol.JarScanner.trimPrefixes;

import com.datadog.debugger.agent.AllowListHelper;
import com.datadog.debugger.agent.Configuration;
import com.datadog.debugger.util.MoshiHelper;
import com.squareup.moshi.JsonAdapter;
import datadog.remoteconfig.ConfigurationChangesListener;
import datadog.remoteconfig.state.ParsedConfigKey;
import datadog.remoteconfig.state.ProductListener;
import datadog.trace.api.Config;
import datadog.trace.util.Strings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import okio.Okio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SymDBEnablement implements ProductListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(SymDBEnablement.class);
  private static final Pattern COMMA_PATTERN = Pattern.compile(",");
  private static final JsonAdapter<SymDbRemoteConfigRecord> SYM_DB_JSON_ADAPTER =
      MoshiHelper.createMoshiConfig().adapter(SymDbRemoteConfigRecord.class);

  private final Instrumentation instrumentation;
  private final Config config;
  private final SymbolAggregator symbolAggregator;
  private SymbolExtractionTransformer symbolExtractionTransformer;
  private long lastUploadTimestamp;

  public SymDBEnablement(
      Instrumentation instrumentation, Config config, SymbolAggregator symbolAggregator) {
    this.instrumentation = instrumentation;
    this.config = config;
    this.symbolAggregator = symbolAggregator;
  }

  @Override
  public void accept(
      ParsedConfigKey configKey,
      byte[] content,
      ConfigurationChangesListener.PollingRateHinter pollingRateHinter)
      throws IOException {
    if (configKey.getConfigId().equals("symDb")) {
      SymDbRemoteConfigRecord symDb = deserializeSymDb(content);
      if (symDb.isUploadSymbols()) {
        startSymbolExtraction();
      } else {
        stopSymbolExtraction();
      }
    } else {
      throw new IOException("unsupported configuration id " + configKey.getConfigId());
    }
  }

  @Override
  public void remove(
      ParsedConfigKey configKey, ConfigurationChangesListener.PollingRateHinter pollingRateHinter)
      throws IOException {}

  @Override
  public void commit(ConfigurationChangesListener.PollingRateHinter pollingRateHinter) {}

  private static SymDbRemoteConfigRecord deserializeSymDb(byte[] content) throws IOException {
    return SYM_DB_JSON_ADAPTER.fromJson(
        Okio.buffer(Okio.source(new ByteArrayInputStream(content))));
  }

  public void stopSymbolExtraction() {
    LOGGER.debug("Stopping symbol extraction.");
    instrumentation.removeTransformer(symbolExtractionTransformer);
  }

  public void startSymbolExtraction() {
    LOGGER.debug("Starting symbol extraction...");
    if (lastUploadTimestamp > 0) {
      LOGGER.debug(
          "Last upload was on {}",
          LocalDateTime.ofInstant(
              Instant.ofEpochMilli(lastUploadTimestamp), ZoneId.systemDefault()));
      return;
    }
    String includes = config.getDebuggerSymbolIncludes();
    AllowListHelper allowListHelper = new AllowListHelper(buildFilterList(includes));
    symbolExtractionTransformer =
        new SymbolExtractionTransformer(allowListHelper, symbolAggregator);
    instrumentation.addTransformer(symbolExtractionTransformer, true);
    extractSymbolForLoadedClasses(allowListHelper);
    lastUploadTimestamp = System.currentTimeMillis();
  }

  private void extractSymbolForLoadedClasses(AllowListHelper allowListHelper) {
    Class<?>[] classesToExtract = null;
    try {
      classesToExtract =
          Arrays.stream(instrumentation.getAllLoadedClasses())
              .filter(clazz -> allowListHelper.isAllowed(clazz.getTypeName()))
              .filter(instrumentation::isModifiableClass)
              .toArray(Class<?>[]::new);
    } catch (Throwable ex) {
      LOGGER.debug("Failed to get all loaded classes", ex);
      return;
    }
    Set<String> alreadyScannedJars = new HashSet<>();
    byte[] buffer = new byte[4096];
    ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
    for (Class<?> clazz : classesToExtract) {
      Path jarPath;
      try {
        jarPath = JarScanner.extractJarPath(clazz);
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
      if (jarPath == null) {
        continue;
      }
      if (!Files.exists(jarPath)) {
        continue;
      }
      if (alreadyScannedJars.contains(jarPath.toString())) {
        continue;
      }
      try {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
          jarFile.stream()
              .filter(jarEntry -> jarEntry.getName().endsWith(".class"))
              .filter(
                  jarEntry ->
                      allowListHelper.isAllowed(
                          Strings.getClassName(trimPrefixes(jarEntry.getName()))))
              .forEach(jarEntry -> parseJarEntry(jarEntry, jarFile, jarPath, baos, buffer));
        }
        alreadyScannedJars.add(jarPath.toString());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void parseJarEntry(
      JarEntry jarEntry, JarFile jarFile, Path jarPath, ByteArrayOutputStream baos, byte[] buffer) {
    LOGGER.debug("parsing jarEntry class: {}", jarEntry.getName());
    try {
      InputStream inputStream = jarFile.getInputStream(jarEntry);
      int readBytes;
      baos.reset();
      while ((readBytes = inputStream.read(buffer)) != -1) {
        baos.write(buffer, 0, readBytes);
      }
      symbolAggregator.parseClass(jarEntry.getName(), baos.toByteArray(), jarPath.toString());
    } catch (IOException ex) {
      LOGGER.debug("Exception during parsing jarEntry class: {}", jarEntry.getName(), ex);
    }
  }

  private Configuration.FilterList buildFilterList(String includes) {
    if (includes == null || includes.isEmpty()) {
      return null;
    }
    String[] includeParts = COMMA_PATTERN.split(includes);
    return new Configuration.FilterList(Arrays.asList(includeParts), Collections.emptyList());
  }
}
