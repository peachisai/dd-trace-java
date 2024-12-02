package datadog.smoketest

import datadog.trace.agent.test.utils.PortUtils
import datadog.trace.test.util.Flaky
import okhttp3.Request
import spock.lang.Shared

@Flaky
class WildflySmokeTest extends AbstractServerSmokeTest {

  @Shared
  File wildflyDirectory = new File(System.getProperty("datadog.smoketest.wildflyDir"))
  @Shared
  int httpsPort = PortUtils.randomOpenPort()
  @Shared
  int managementPort = PortUtils.randomOpenPort()

  @Override
  ProcessBuilder createProcessBuilder() {
    ProcessBuilder processBuilder =
      new ProcessBuilder("${wildflyDirectory}/bin/standalone.sh")
    processBuilder.directory(wildflyDirectory)
    List<String> javaOpts = [
      *defaultJavaProperties,
      "-Djboss.http.port=${httpPort}",
      "-Djboss.https.port=${httpsPort}",
      "-Djboss.management.http.port=${managementPort}"
    ]
    processBuilder.environment().put("JAVA_OPTS", javaOpts.collect({ it.replace(' ', '\\ ')}).join(' '))
    return processBuilder
  }

  def cleanupSpec() {
    ProcessBuilder processBuilder = new ProcessBuilder(
      "${wildflyDirectory}/bin/jboss-cli.sh",
      "--connect",
      "--controller=localhost:${managementPort}",
      "command=:shutdown")
    processBuilder.directory(wildflyDirectory)
    Process process = processBuilder.start()
    process.waitFor()
  }

  def "default home page #n th time"() {
    setup:
    String url = "http://localhost:$httpPort/"
    def request = new Request.Builder().url(url).get().build()

    when:
    def response = client.newCall(request).execute()

    then:
    def responseBodyStr = response.body().string()
    responseBodyStr != null
    responseBodyStr.contains("Your WildFly instance is running.")
    response.body().contentType().toString().contains("text/html")
    response.code() == 200

    where:
    n << (1..200)
  }

  def "spring context loaded successfully"() {
    setup:
    String url = "http://localhost:$httpPort/war/hello"
    def request = new Request.Builder().url(url).get().build()

    when:
    def response = client.newCall(request).execute()

    then:
    def responseBodyStr = response.body().string()
    responseBodyStr != null
    responseBodyStr.contentEquals("hello world")
    response.code() == 200
  }
}
