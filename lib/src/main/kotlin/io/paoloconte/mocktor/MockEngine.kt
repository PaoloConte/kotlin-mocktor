package io.paoloconte.mocktor

import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.date.*
import io.ktor.utils.io.*

object MockEngine: HttpClientEngineBase("mock-engine") {
    override val config: HttpClientEngineConfig = object : HttpClientEngineConfig() {}

    override val supportedCapabilities: Set<HttpClientEngineCapability<out Any>> = setOf(
        HttpTimeoutCapability,
        WebSocketCapability,
        WebSocketExtensionsCapability
    )

    private val handlers: MutableList<RequestMatcher> = mutableListOf()

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
        for (matcher in handlers) {
            if (matcher.matches(data)) {
                return HttpResponseData(
                    statusCode = matcher.responseStatus,
                    requestTime = GMTDate(),
                    headers = headersOf("Content-Type", matcher.responseContentType.toString()),
                    body = ByteReadChannel(matcher.responseContent?.invoke(data) ?: ByteArray(0)),
                    version = HttpProtocolVersion.HTTP_1_1,
                    callContext = callContext(),
                )
            }
        }

        return HttpResponseData(
            statusCode = HttpStatusCode.NotFound,
            requestTime = GMTDate(),
            headers = headersOf(),
            body = ByteReadChannel(ByteArray(0)),
            version = HttpProtocolVersion.HTTP_1_1,
            callContext = callContext(),
        )
    }
    

}