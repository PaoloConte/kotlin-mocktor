package io.paoloconte.mocktor

import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import io.paoloconte.mocktor.MockEngine.INITIAL_STATE
import io.paoloconte.mocktor.MockEngine.noMatchStatusCode
import org.slf4j.LoggerFactory

/**
 * Represents a recorded HTTP call made through the [MockEngine].
 *
 * @property request The original HTTP request data.
 * @property response The HTTP response data, or null if the request threw an exception.
 */
data class RecordedCall(
    val request: HttpRequestData,
    val response: HttpResponseData? = null
)

/**
 * A mock HTTP client engine for Ktor that allows stubbing HTTP responses and verifying requests.
 *
 * MockEngine intercepts HTTP requests and returns predefined responses based on configured matchers.
 * It also records all calls for later verification.
 *
 * Example usage:
 * ```kotlin
 * val client = HttpClient(MockEngineFactory)
 *
 * MockEngine.get("/users") {
 *     response {
 *         status(HttpStatusCode.OK)
 *         body("""[{"id": 1, "name": "John"}]""")
 *     }
 * }
 *
 * // Make requests using the client...
 *
 * MockEngine.verify { path equalTo "/users" }
 * MockEngine.clear()
 * ```
 *
 * @see MockEngineFactory
 * @see RequestMatcher
 */
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

    /**
     * The HTTP status code returned when no handler matches a request.
     * Defaults to [HttpStatusCode.NotFound].
     */
    var noMatchStatusCode: HttpStatusCode = defaultStatusCode

    /**
     * The initial state value used for stateful request matching.
     */
    const val INITIAL_STATE = "INITIAL_STATE"
    internal var state: String = INITIAL_STATE

    /**
     * Clears all registered handlers, recorded calls, resets the state to [INITIAL_STATE],
     * and resets [noMatchStatusCode] to [HttpStatusCode.NotFound].
     *
     * Call this method between tests to ensure a clean state.
     */
    fun clear() {
        handlers.clear()
        recordedCalls.clear()
        state = INITIAL_STATE
        noMatchStatusCode = defaultStatusCode
    }

    /**
     * Registers a handler for GET requests.
     *
     * @param path Optional URL path to match exactly. If null, matches any path.
     * @param builder Configuration block for request matching and response definition.
     */
    fun get(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Get, path).apply {  builder() }.build())
    }

    /**
     * Registers a handler for POST requests.
     *
     * @param path Optional URL path to match exactly. If null, matches any path.
     * @param builder Configuration block for request matching and response definition.
     */
    fun post(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Post, path).apply {  builder() }.build())
    }

    /**
     * Registers a handler for PUT requests.
     *
     * @param path Optional URL path to match exactly. If null, matches any path.
     * @param builder Configuration block for request matching and response definition.
     */
    fun put(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Put, path).apply {  builder() }.build())
    }

    /**
     * Registers a handler for DELETE requests.
     *
     * @param path Optional URL path to match exactly. If null, matches any path.
     * @param builder Configuration block for request matching and response definition.
     */
    fun delete(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Delete, path).apply {  builder() }.build())
    }

    /**
     * Registers a handler for PATCH requests.
     *
     * @param path Optional URL path to match exactly. If null, matches any path.
     * @param builder Configuration block for request matching and response definition.
     */
    fun patch(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Patch, path).apply {  builder() }.build())
    }

    /**
     * Registers a handler for HEAD requests.
     *
     * @param path Optional URL path to match exactly. If null, matches any path.
     * @param builder Configuration block for request matching and response definition.
     */
    fun head(path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(HttpMethod.Head, path).apply {  builder() }.build())
    }

    /**
     * Registers a handler for any HTTP method.
     *
     * @param method Optional HTTP method to match. If null, matches any method.
     * @param path Optional URL path to match exactly. If null, matches any path.
     * @param builder Configuration block for request matching and response definition.
     */
    fun on(method: HttpMethod? = null, path: String? = null, builder: RequestMatcher.Builder.() -> Unit) {
        handlers.add(RequestMatcher.Builder(method, path).apply { builder() }.build())
    }

    /**
     * Verifies that requests matching the given criteria were made.
     *
     * @param count Expected number of matching requests. If null, verifies at least one match.
     * @param builder Configuration block for request matching criteria.
     * @throws AssertionError If the expected number of matching requests was not found.
     */
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

    /**
     * Returns a list of all recorded calls made through this mock engine.
     *
     * @return An immutable copy of all recorded calls.
     */
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
            sb.append("${index + 1}. [${handler.method?.toString() ?: ""} ${handler.host?.toString() ?: ""} ${handler.path?.toString() ?: ""}] -> ${mismatchDescriptions[index]}\n")
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