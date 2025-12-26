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

class RequestMatcher(
    val method: ValueMatcher<HttpMethod>?,
    val path: ValueMatcher<String>?,
    val matcher: ((HttpRequestData) -> Boolean)?,
    val requestContentType: ValueMatcher<String>?,
    val contentMatcher: ContentMatcher?,
    val responseStatus: HttpStatusCode,
    val responseContentType: ContentType,
    val responseContent: ((HttpRequestData) -> ByteArray)?,
    val expectedState: String?,
    val setState: String?,
    val requestHeaders: HeadersMatcher,
    val responseHeaders: Map<String, String>,
    val responseException: Throwable?,
    val queryParams: QueryParamsMatcher,
    val formParams: FormUrlEncodedContentMatcher,
) {
    class Builder(method: HttpMethod? = null, path: String? = null) {
        
        private val request = RequestBuilder(method, path)
        private val response = ResponseBuilder()
        
        fun withState(state: String) {
            request.expectedState = state
        }
        
        fun request(builder: RequestBuilder.() -> Unit): Builder {
            request.apply { builder() }
            return this
        }
        
        fun response(builder: ResponseBuilder.() -> Unit): Builder {
            response.apply { builder() }
            return this
        }

        fun build(): RequestMatcher = RequestMatcher(
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
        
        class RequestBuilder(
            initialMethod: HttpMethod? = null,
            initialPath: String? = null,
        ) {
            val method = Matchable<HttpMethod>().apply { initialMethod?.let { this equalTo it } }
            val path = StringMatchable().apply { initialPath?.let { this equalTo it } }
            val queryParams = QueryParamsMatcher()
            val formParams = FormUrlEncodedContentMatcher()
            val contentType = ContentTypesMatchable()
            val headers = HeadersMatcher()
            var body = BodyMatchable()
            internal var matcher: ((HttpRequestData) -> Boolean)? = null
            internal var expectedState: String? = null
            
            fun matching(matcher: (HttpRequestData) -> Boolean) {
                this.matcher = matcher
            }

            fun withBodyMatcher(contentMatcher: ContentMatcher) {
                this.body.matcher = contentMatcher
            }

            fun strictQueryParams() {
                queryParams.ignoreUnknownParams = false
            }
            
            fun strictFormParams() {
                formParams.ignoreUnknownKeys = false
            }

        }

        class ResponseBuilder {
            internal var status: HttpStatusCode = HttpStatusCode.OK
            internal var contentType: ContentType = ContentType.Application.Json
            internal var bodyContent: ((HttpRequestData) -> ByteArray)? = null
            internal var newState: String? = null
            internal var headers: MutableMap<String, String> = mutableMapOf()
            internal var exception: Throwable? = null

            fun setState(state: String) {
                newState = state
            }

            fun status(status: HttpStatusCode) {
                this.status = status
            }

            fun contentType(contentType: ContentType) {
                this.contentType = contentType
            }

            fun contentType(contentType: String) {
                this.contentType = ContentType.parse(contentType)
            }

            fun header(name: String, value: String) {
                headers[name] = value
            }

            fun throws(exception: Throwable) {
                this.exception = exception
            }

            fun bodyFromResource(path: String) {
                bodyContent = { r ->
                    this.javaClass.getResource(path)?.readBytes()
                        ?: error("Unable to load resource file '$path'")
                }
            }

            fun body(content: String) {
                bodyContent = { r ->
                    content.toByteArray(Charsets.UTF_8)
                }
            }

            fun body(content: (HttpRequestData) -> ByteArray) {
                bodyContent = content
            }
        }
    }

    fun matches(data: HttpRequestData, currentState: String? = null): MatchResult {
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

    fun description(): String {
        val parts = mutableListOf<String>()
        if (method != null) parts.add("method=$method")
        if (path != null) parts.add("path=$path")
        if (requestHeaders.isNotEmpty()) parts.add("headers=<specified>")
        if (queryParams.isNotEmpty()) parts.add("queryParams=<specified>")
        if (contentMatcher != null) parts.add("body=<specified>")
        return parts.joinToString(", ").ifEmpty { "any" }
    }

}