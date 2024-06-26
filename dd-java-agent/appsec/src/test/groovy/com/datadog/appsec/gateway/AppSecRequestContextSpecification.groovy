package com.datadog.appsec.gateway


import com.datadog.appsec.config.CurrentAppSecConfig
import com.datadog.appsec.event.data.KnownAddresses
import com.datadog.appsec.event.data.MapDataBundle
import com.datadog.appsec.report.AppSecEvent
import com.datadog.appsec.stack_trace.StackTraceCollection
import com.datadog.appsec.stack_trace.StackTraceEvent
import com.datadog.appsec.test.StubAppSecConfigService
import datadog.trace.test.util.DDSpecification
import io.sqreen.powerwaf.Additive
import io.sqreen.powerwaf.Powerwaf
import io.sqreen.powerwaf.PowerwafContext

class AppSecRequestContextSpecification extends DDSpecification {

  AppSecRequestContext ctx = new AppSecRequestContext()

  void 'implements DataBundle'() {
    when:
    ctx.addAll(MapDataBundle.of(KnownAddresses.REQUEST_URI_RAW, '/a'))

    then:
    ctx.size() == 1
    ctx.get(KnownAddresses.REQUEST_URI_RAW) == '/a'
    ctx.allAddresses as List == [KnownAddresses.REQUEST_URI_RAW]
    ctx.hasAddress(KnownAddresses.REQUEST_URI_RAW)

    when:
    def iter = ctx.iterator()

    then:
    iter.hasNext()

    when:
    def elem = iter.next()

    then:
    elem.key == KnownAddresses.REQUEST_URI_RAW
    elem.value == '/a'
  }

  void 'it is closeable'() {
    expect:
    assert ctx.respondsTo('close')

    when:
    ctx.close()

    then:
    notThrown(Exception)
  }

  void 'adding headers after they are said to be finished is forbidden'() {
    when:
    ctx.finishRequestHeaders()

    and:
    ctx.addRequestHeader('a', 'b')

    then:
    ctx.finishedRequestHeaders
    thrown(IllegalStateException)

    when:
    ctx.addCookies(a: ['b'])

    then:
    thrown(IllegalStateException)
  }

  void 'adding uri a second time is forbidden'() {
    when:
    ctx.rawURI = '/a'
    ctx.rawURI = '/b'

    then:
    thrown(IllegalStateException)
    ctx.savedRawURI == '/a'
  }

  void 'saves cookies and other headers'() {
    when:
    ctx.addCookies([a: ['c']])
    ctx.addRequestHeader('user-agent', 'foo')

    then:
    ctx.requestHeaders['user-agent'] == ['foo']
    ctx.cookies == [a: ['c']]
  }

  void 'can save the URI'() {
    when:
    ctx.savedRawURI = '/a'

    then:
    ctx.savedRawURI == '/a'
  }

  void 'can collect events'() {
    when:
    ctx.reportEvents([new AppSecEvent(), new AppSecEvent()])
    def events = ctx.transferCollectedEvents()

    then:
    events.size() == 2
    events[0] != null
    events[1] != null

    when:
    ctx.reportEvents([new AppSecEvent()])
    events = ctx.transferCollectedEvents()

    then:
    events.size() == 1
    events[0] != null
  }

  void 'collect events when none reported'() {
    expect:
    ctx.transferCollectedEvents().empty
  }

  void 'can collect stack traces'() {
    setup:
    StackTraceElement element = new StackTraceElement('class', 'method', 'file', 1)
    StackTraceEvent.Frame frame = new StackTraceEvent.Frame(element, 1)
    StackTraceEvent event = new StackTraceEvent('id', 'message', [frame])

    when:
    ctx.reportStackTrace(event)
    StackTraceCollection collection = ctx.transferStackTracesCollection()

    then:
    collection.exploit.size() == 1
    collection.exploit[0].id == 'id'
    collection.exploit[0].message == 'message'
    collection.exploit[0].language == 'java'
    collection.exploit[0].frames.size() == 1
    collection.exploit[0].frames[0].id == 1
    collection.exploit[0].frames[0].text == 'class.method(file:1)'
    collection.exploit[0].frames[0].file == 'file'
    collection.exploit[0].frames[0].line == 1
    collection.exploit[0].frames[0].class_name == 'class'
    collection.exploit[0].frames[0].function == 'method'
  }

  void 'collect stack traces when none reported'() {
    expect:
    ctx.transferStackTracesCollection() == null
  }

  void 'headers allow list should contains only lowercase names'() {
    expect:
    headers.each {
      assert it == it.toLowerCase(): "REASON: Allow header name \"$it\" MUST be lowercase"
    }

    where:
    headers                                                 | name
    AppSecRequestContext.DEFAULT_REQUEST_HEADERS_ALLOW_LIST | 'Default request headers'
    AppSecRequestContext.REQUEST_HEADERS_ALLOW_LIST         | 'Request headers'
    AppSecRequestContext.RESPONSE_HEADERS_ALLOW_LIST        | 'Response headers'
  }

  void 'basic headers collection test'() {
    when:
    ctx.addRequestHeader('Host', '127.0.0.1')
    ctx.addRequestHeader('Content-Type', 'text/html; charset=UTF-8')
    ctx.addRequestHeader('Custom-Header', 'value1')
    ctx.addRequestHeader('Accept', 'application/json')

    then:
    ctx.requestHeaders == [
      'host': ['127.0.0.1'],
      'content-type': ['text/html; charset=UTF-8'],
      'custom-header': ['value1'],
      'accept': ['application/json']] as Map
  }

  void 'null headers should be ignored'() {
    when:
    ctx.addRequestHeader(null, 'value')
    ctx.addRequestHeader('key', null)

    then:
    ctx.requestHeaders.isEmpty()
  }

  void 'concat multiple values for same header'() {
    when:
    ctx.addRequestHeader('Custom-Header', 'value1')
    ctx.addRequestHeader('CUSTOM-HEADER', 'value2')
    ctx.addRequestHeader('Accept', 'application/json')
    ctx.addRequestHeader('accept', 'application/xml')

    then:
    ctx.requestHeaders == [
      'custom-header': ['value1', 'value2'],
      'accept': ['application/json', 'application/xml']] as Map
  }

  private Additive createAdditive() {
    Powerwaf.initialize false
    def service = new StubAppSecConfigService()
    service.init()
    CurrentAppSecConfig config = service.lastConfig['waf']
    String uniqueId = UUID.randomUUID() as String
    config.dirtyStatus.markAllDirty()
    PowerwafContext context = Powerwaf.createContext(uniqueId, config.mergedUpdateConfig.rawConfig)
    new Additive(context)
  }

  void 'close closes the additive'() {
    setup:
    def additive = createAdditive()

    when:
    ctx.additive = additive
    ctx.close()

    then:
    ctx.additive == null
    !additive.online
  }

  void 'test isThrottled'(){
    setup:
    def rateLimiter = Mock(RateLimiter)
    def appSecRequestContext = new AppSecRequestContext()

    when: 'rate limiter is called and throttled is set'
    def result = appSecRequestContext.isThrottled(rateLimiter)

    then:
    1 * rateLimiter.isThrottled() >> true
    assert result

    when: 'rate limiter is not called more than once per appsec context returns first result'
    def result2 = appSecRequestContext.isThrottled(rateLimiter)

    then:
    0 * rateLimiter.isThrottled()
    result == result2
  }
}
