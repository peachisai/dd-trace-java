package datadog.trace.instrumentation.aws.v0;

import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activateSpan;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.propagate;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.startSpan;
import static datadog.trace.instrumentation.aws.v0.AwsSdkClientDecorator.AWS_HTTP;
import static datadog.trace.instrumentation.aws.v0.AwsSdkClientDecorator.DECORATE;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.handlers.HandlerContextKey;
import com.amazonaws.handlers.RequestHandler2;
import datadog.trace.api.Config;
import datadog.trace.api.TracePropagationStyle;
import datadog.trace.bootstrap.instrumentation.api.AgentSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Tracing Request Handler */
public class TracingRequestHandler extends RequestHandler2 {

  public static final HandlerContextKey<AgentSpan> SPAN_CONTEXT_KEY =
      new HandlerContextKey<>("DatadogSpan"); // same as OnErrorDecorator.SPAN_CONTEXT_KEY

  private static final Logger log = LoggerFactory.getLogger(TracingRequestHandler.class);

  @Override
  public AmazonWebServiceRequest beforeMarshalling(final AmazonWebServiceRequest request) {
    return request;
  }

  @Override
  public void beforeRequest(final Request<?> request) {
    final AgentSpan span = startSpan(AWS_HTTP);
    DECORATE.afterStart(span);
    DECORATE.onRequest(span, request);
    request.addHandlerContext(SPAN_CONTEXT_KEY, span);
    if (Config.get().isAwsPropagationEnabled()) {
      try {
        propagate().inject(span, request, DECORATE, TracePropagationStyle.XRAY);
      } catch (Throwable e) {
        log.warn("Unable to inject trace header", e);
      }
    }
    // This scope will be closed by AwsHttpClientInstrumentation
    activateSpan(span);
  }

  @Override
  public void afterResponse(final Request<?> request, final Response<?> response) {
    final AgentSpan span = request.getHandlerContext(SPAN_CONTEXT_KEY);
    if (span != null) {
      request.addHandlerContext(SPAN_CONTEXT_KEY, null);
      DECORATE.onResponse(span, response);
      DECORATE.beforeFinish(span);
      span.finish();
    }
  }

  @Override
  public void afterError(final Request<?> request, final Response<?> response, final Exception e) {
    final AgentSpan span = request.getHandlerContext(SPAN_CONTEXT_KEY);
    if (span != null) {
      request.addHandlerContext(SPAN_CONTEXT_KEY, null);
      DECORATE.onError(span, e);
      DECORATE.beforeFinish(span);
      span.finish();
    }
  }
}
