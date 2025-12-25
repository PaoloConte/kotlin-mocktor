package io.paoloconte.mocktor

import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import org.slf4j.LoggerFactory

data class RecordedCall(
    val request: HttpRequestData,
    val response: HttpResponseData? = null
)

object MockEngine: HttpClientEngineBase("mock-engine") {

    override val config: HttpClientEngineConfig = object : HttpClientEngineConfig() {}

    override val supportedCapabilities: Set<HttpClientEngineCapability<out Any>> = setOf(
        HttpTimeoutCapability,
        WebSocketCapability,
        WebSocketExtensionsCapability
    )

    private val logger = LoggerFactory.getLogger(MockEngine::class.java)
    private val handlers: MutableList<RequestMatcher> = mutableListOf()
    private val recordedCalls: MutableList<RecordedCall> = mutableListOf()
    private val defaultStatusCode = HttpStatusCode.NotFound
    var noMatchStatusCode: HttpStatusCode = defaultStatusCode

    const val INITIAL_STATE = "INITIAL_STATE"
    internal var state: String = INITIAL_STATE

    fun clear() {
        handlers.clear()
        recordedCalls.clear()
        state = INITIAL_STATE
        noMatchStatusCode = defaultStatusCode
    }

    fun get(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Get, path).apply {  builder() }.build())
    }

    fun post(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Post, path).apply {  builder() }.build())
    }

    fun put(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Put, path).apply {  builder() }.build())
    }

    fun delete(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Delete, path).apply {  builder() }.build())
    }

    fun patch(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Patch, path).apply {  builder() }.build())
    }

    fun head(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Head, path).apply {  builder() }.build())
    }

    fun on(method: HttpMethod? = null, path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(method, path).apply { builder() }.build())
    }

    fun verify(count: Int? = null, builder: RequestMatcher.Builder.RequestBuilder.() -> Unit) {
        val matcher = RequestMatcher.Builder().request(builder).build()
        val matchingCalls = recordedCalls.filter { matcher.matches(it.request) is MatchResult.Match }

        if (count != null && matchingCalls.size != count) {
            throw AssertionError(
                "Expected $count request(s) matching [${matcher.description()}], but found ${matchingCalls.size}"
            )
        }

        if (count == null && matchingCalls.isEmpty()) {
            throw AssertionError(
                "Expected at least 1 request matching [${matcher.description()}], but found none"
            )
        }
    }

    fun calls(): List<RecordedCall> = recordedCalls.toList()

    fun requests(): List<RecordedCall> = recordedCalls.toList()

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        logger.trace("Handling request: {} {}", data.method, data.url)

        val mismatchDescriptions = mutableListOf<String>()

        for (matcher in handlers) {
            when (val result = matcher.matches(data, state)) {
                is MatchResult.Match -> {
                    if (matcher.setState != null) {
                        state = matcher.setState
                    }

                    if (matcher.responseException != null) {
                        recordedCalls.add(RecordedCall(data))
                        throw matcher.responseException
                    }

                    val headers = headers {
                        matcher.responseHeaders.forEach { append(it.key, it.value) }
                        append(HttpHeaders.ContentType, matcher.responseContentType.toString())
                    }

                    val response = HttpResponseData(
                        statusCode = matcher.responseStatus,
                        requestTime = GMTDate(),
                        headers = headers,
                        body = ByteReadChannel(matcher.responseContent?.invoke(data) ?: ByteArray(0)),
                        version = HttpProtocolVersion.HTTP_1_1,
                        callContext = callContext(),
                    )

                    recordedCalls.add(RecordedCall(data, response))
                    return response
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

        val response = HttpResponseData(
            statusCode = noMatchStatusCode,
            requestTime = GMTDate(),
            headers = headersOf(),
            body = ByteReadChannel(mismatchReport.toByteArray()),
            version = HttpProtocolVersion.HTTP_1_1,
            callContext = callContext(),
        )

        recordedCalls.add(RecordedCall(data, response))
        return response
    }


}