package io.paoloconte.mocktor

import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import org.slf4j.LoggerFactory

object MockEngine: HttpClientEngineBase("mock-engine") {
    private val logger = LoggerFactory.getLogger(MockEngine::class.java)

    override val config: HttpClientEngineConfig = object : HttpClientEngineConfig() {}

    override val supportedCapabilities: Set<HttpClientEngineCapability<out Any>> = setOf(
        HttpTimeoutCapability,
        WebSocketCapability,
        WebSocketExtensionsCapability
    )

    private val handlers: MutableList<RequestMatcher> = mutableListOf()
    
    var noMatchStatusCode: HttpStatusCode = HttpStatusCode.NotFound

    fun clear() = handlers.clear()

    fun get(path: String, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Get, path).apply {  builder() }.build())
    }
    
    fun post(path: String, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Post, path).apply {  builder() }.build())
    }
    
    fun put(path: String, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Put, path).apply {  builder() }.build())
    }
    
    fun delete(path: String, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Delete, path).apply {  builder() }.build())
    }
    
    fun patch(path: String, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Patch, path).apply {  builder() }.build())
    }
    
    fun head(path: String, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Head, path).apply {  builder() }.build())
    }

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        logger.trace("Handling request: {} {}", data.method, data.url)
        val mismatchDescriptions = mutableListOf<String>()

        for (matcher in handlers) {
            when (val result = matcher.matches(data)) {
                is MatchResult.Match -> {
                    logger.trace("Matched handler: {} {}", matcher.method, matcher.path)
                    return HttpResponseData(
                        statusCode = matcher.responseStatus,
                        requestTime = GMTDate(),
                        headers = headersOf("Content-Type", matcher.responseContentType.toString()),
                        body = ByteReadChannel(matcher.responseContent?.invoke(data) ?: ByteArray(0)),
                        version = HttpProtocolVersion.HTTP_1_1,
                        callContext = callContext(),
                    )
                }
                is MatchResult.Mismatch -> {
                    mismatchDescriptions.add(result.reason)
                }
            }
        }

        logger.trace("No matching handler found for: {} {}", data.method, data.url)
        val sb = StringBuilder("No matching handler found. Registered handlers:\n")
        handlers.forEachIndexed { index, handler ->
            sb.append("${index + 1}. [${handler.method} ${handler.path}] -> ${mismatchDescriptions[index]}\n")
        }
        val mismatchReport = sb.toString()
        logger.error(mismatchReport)

        return HttpResponseData(
            statusCode = noMatchStatusCode,
            requestTime = GMTDate(),
            headers = headersOf(),
            body = ByteReadChannel(mismatchReport.toByteArray()),
            version = HttpProtocolVersion.HTTP_1_1,
            callContext = callContext(),
        )
    }
    

}