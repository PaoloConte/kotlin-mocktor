package io.paoloconte.mocktor

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.paoloconte.mocktor.MatchResult.Match
import io.paoloconte.mocktor.MatchResult.Mismatch
import io.paoloconte.mocktor.contentMatchers.ContentMatcher
import io.paoloconte.mocktor.contentMatchers.DefaultContentMatcher
import io.paoloconte.mocktor.contentMatchers.FormUrlEncodedContentMatcher
import io.paoloconte.mocktor.valueMatchers.BodyMatchable
import io.paoloconte.mocktor.valueMatchers.ContentTypesMatchable
import io.paoloconte.mocktor.valueMatchers.HeadersMatcher
import io.paoloconte.mocktor.valueMatchers.Matchable
import io.paoloconte.mocktor.valueMatchers.QueryParamsMatcher
import io.paoloconte.mocktor.valueMatchers.StringMatchable
import io.paoloconte.mocktor.valueMatchers.ValueMatcher

class RequestMatcher private constructor(
    internal val method: ValueMatcher<HttpMethod>?,
    internal val path: ValueMatcher<String>?,
    internal val matcher: ((HttpRequestData) -> Boolean)?,
    internal val requestContentType: ValueMatcher<String>?,
    internal val contentMatcher: ContentMatcher?,
    internal val responseStatus: HttpStatusCode,
    internal val responseContentType: ContentType,
    internal val responseContent: ((HttpRequestData) -> ByteArray)?,
    internal val expectedState: String?,
    internal val setState: String?,
    internal val requestHeaders: HeadersMatcher,
    internal val responseHeaders: Map<String, String>,
    internal val responseException: Throwable?,
    internal val queryParams: QueryParamsMatcher,
    internal val formParams: FormUrlEncodedContentMatcher,
) {
    
    class Builder internal constructor(method: HttpMethod? = null, path: String? = null) {

        private val request = RequestBuilder(method, path)
        private val response = ResponseBuilder()

        /**
         * Sets the required state for this handler to match.
         * Used for stateful mocking scenarios.
         *
         * @param state The state value that must be current for this handler to match.
         */
        fun withState(state: String) {
            request.expectedState = state
        }

        /**
         * Configures request matching criteria.
         *
         * @param builder Configuration block for request matching.
         * @return This builder for chaining.
         */
        fun request(builder: RequestBuilder.() -> Unit): Builder {
            request.apply { builder() }
            return this
        }

        /**
         * Configures the mock response.
         *
         * @param builder Configuration block for response definition.
         * @return This builder for chaining.
         */
        fun response(builder: ResponseBuilder.() -> Unit): Builder {
            response.apply { builder() }
            return this
        }

        /**
         * Builds the [RequestMatcher] instance.
         *
         * @return The configured request matcher.
         */
        internal fun build(): RequestMatcher = RequestMatcher(
            path = request.path.matcher,
            method = request.method.matcher,
            matcher = request.matcher,
            contentMatcher = request.body.matcher,
            requestContentType = request.contentType.matcher,
            requestHeaders = request.headers,
            responseStatus = response.status,
            responseContentType = response.contentType,
            responseContent = response.bodyContent,
            expectedState = request.expectedState,
            setState = response.newState,
            responseHeaders = response.headers,
            responseException = response.exception,
            queryParams = request.queryParams,
            formParams = request.formParams,
        )
        
        class RequestBuilder internal constructor(
            initialMethod: HttpMethod? = null,
            initialPath: String? = null,
        ) {
            /** Matcher for the HTTP method. */
            val method = Matchable<HttpMethod>().apply { initialMethod?.let { this equalTo it } }

            /** Matcher for the URL path. */
            val path = StringMatchable().apply { initialPath?.let { this equalTo it } }

            /** Matcher for URL query parameters. */
            val queryParams = QueryParamsMatcher()

            /** Matcher for form-urlencoded body parameters. */
            val formParams = FormUrlEncodedContentMatcher()

            /** Matcher for the request content type. */
            val contentType = ContentTypesMatchable()

            /** Matcher for request headers. */
            val headers = HeadersMatcher()

            /** Matcher for the request body. */
            var body = BodyMatchable()

            internal var matcher: ((HttpRequestData) -> Boolean)? = null
            internal var expectedState: String? = null

            /**
             * Adds a custom matching function for arbitrary request validation.
             *
             * @param matcher Function that returns true if the request matches.
             */
            fun matching(matcher: (HttpRequestData) -> Boolean) {
                this.matcher = matcher
            }

            /**
             * Sets a custom content matcher for the request body.
             *
             * @param contentMatcher The content matcher to use.
             */
            fun withBodyMatcher(contentMatcher: ContentMatcher) {
                this.body.matcher = contentMatcher
            }

            /**
             * Enables strict query parameter matching.
             * When enabled, the request must not contain any query parameters
             * that are not explicitly matched.
             */
            fun strictQueryParams() {
                queryParams.ignoreUnknownParams = false
            }

            /**
             * Enables strict form parameter matching.
             * When enabled, the request body must not contain any form parameters
             * that are not explicitly matched.
             */
            fun strictFormParams() {
                formParams.ignoreUnknownKeys = false
            }

        }
        
        class ResponseBuilder internal constructor() {
            internal var status: HttpStatusCode = HttpStatusCode.OK
            internal var contentType: ContentType = ContentType.Application.Json
            internal var bodyContent: ((HttpRequestData) -> ByteArray)? = null
            internal var newState: String? = null
            internal var headers: MutableMap<String, String> = mutableMapOf()
            internal var exception: Throwable? = null

            /**
             * Sets the state to transition to after this handler matches.
             * Used for stateful mocking scenarios.
             *
             * @param state The new state value.
             */
            fun setState(state: String) {
                newState = state
            }

            /**
             * Sets the HTTP status code for the response.
             *
             * @param status The HTTP status code. Defaults to [HttpStatusCode.OK].
             */
            fun status(status: HttpStatusCode) {
                this.status = status
            }

            /**
             * Sets the content type for the response.
             *
             * @param contentType The content type. Defaults to [ContentType.Application.Json].
             */
            fun contentType(contentType: ContentType) {
                this.contentType = contentType
            }

            /**
             * Sets the content type for the response from a string.
             *
             * @param contentType The content type string to parse.
             */
            fun contentType(contentType: String) {
                this.contentType = ContentType.parse(contentType)
            }

            /**
             * Adds a header to the response.
             *
             * @param name The header name.
             * @param value The header value.
             */
            fun header(name: String, value: String) {
                headers[name] = value
            }

            /**
             * Configures the handler to throw an exception instead of returning a response.
             *
             * @param exception The exception to throw.
             */
            fun throws(exception: Throwable) {
                this.exception = exception
            }

            /**
             * Sets the response body from a classpath resource file.
             *
             * @param path The resource path (e.g., "/responses/users.json").
             */
            fun bodyFromResource(path: String) {
                bodyContent = { r ->
                    this.javaClass.getResource(path)?.readBytes()
                        ?: error("Unable to load resource file '$path'")
                }
            }

            /**
             * Sets the response body from a string.
             *
             * @param content The response body content.
             */
            fun body(content: String) {
                bodyContent = { r ->
                    content.toByteArray(Charsets.UTF_8)
                }
            }

            /**
             * Sets the response body using a function that receives the request.
             * Useful for dynamic responses based on request data.
             *
             * @param content Function that generates the response body from the request.
             */
            fun body(content: (HttpRequestData) -> ByteArray) {
                bodyContent = content
            }
        }
    }

    /**
     * Checks if the given HTTP request matches all configured criteria.
     *
     * @param data The HTTP request data to match against.
     * @param currentState The current state for stateful matching, or null.
     * @return [MatchResult.Match] if all criteria match, or [MatchResult.Mismatch] with a reason.
     */
    internal fun matches(data: HttpRequestData, currentState: String? = null): MatchResult {
        if (expectedState != null && expectedState != currentState) {
            return Mismatch("State mismatch: expected $expectedState but was $currentState")
        }

        if (method != null && !method.matches(data.method))
            return Mismatch("Method mismatch: expected $method but was ${data.method}")

        if (path != null && !path.matches(data.url.encodedPath))
            return Mismatch("Path mismatch: expected $path but was ${data.url.encodedPath}")

        if (requestContentType != null && !requestContentType.matches(data.body.contentType.toString()))
            return Mismatch("Content-Type mismatch: expected $requestContentType but was ${data.body.contentType}")

        if (requestHeaders.isNotEmpty()) {
            val result = requestHeaders.matches(data.headers)
            if (result is Mismatch) return result
        }

        if (queryParams.isNotEmpty()) {
            val result = queryParams.matches(data.url.parameters)
            if (result is Mismatch) return result
        }

        if (formParams.isNotEmpty()) {
            val result = formParams.matches(data.bodyAsBytesOrNull() ?: ByteArray(0))
            if (result is Mismatch) return result
        }

        if (matcher != null && !matcher(data))
            return Mismatch("Custom matcher failed")

        if (contentMatcher != null) {
            val body = (data.body as? OutgoingContent.ByteArrayContent)?.bytes() ?: return Mismatch("Body mismatch: request body is not available as ByteArray")
            return contentMatcher.matches(body)
        }

        return Match
    }

    /**
     * Returns a human-readable description of the matching criteria.
     *
     * @return A string describing the configured matchers.
     */
    internal fun description(): String {
        val parts = mutableListOf<String>()
        if (method != null) parts.add("method=$method")
        if (path != null) parts.add("path=$path")
        if (requestHeaders.isNotEmpty()) parts.add("headers=<specified>")
        if (queryParams.isNotEmpty()) parts.add("queryParams=<specified>")
        if (contentMatcher != null) parts.add("body=<specified>")
        return parts.joinToString(", ").ifEmpty { "any" }
    }

}