package io.paoloconte.mocktor

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.paoloconte.mocktor.MatchResult.Match
import io.paoloconte.mocktor.MatchResult.Mismatch

class RequestMatcher(
    val method: HttpMethod?,
    val path: String?,
    val matcher: ((HttpRequestData) -> Boolean)?,
    val requestContentType: ContentType?,
    val requestContent: ByteArray?,
    val contentMatcher: ContentMatcher,
    val responseStatus: HttpStatusCode,
    val responseContentType: ContentType,
    val responseContent: ((HttpRequestData) -> ByteArray)?,
    val expectedState: String?,
    val setState: String?,
    val requestHeaders: Map<String, String>,
    val responseHeaders: Map<String, String>,
    val responseException: Throwable?,
    val queryParams: QueryParams?,
) {
    class Builder(var method: HttpMethod?, val path: String?) {
        
        private val request = RequestBuilder()
        private val response = ResponseBuilder()
        
        fun withState(state: String) {
            request.expectedState = state
        }
        
        fun request(builder: RequestBuilder.() -> Unit) {
            request.apply { builder() }
        }
        
        fun response(builder: ResponseBuilder.() -> Unit) {
            response.apply { builder() }
        }

        fun build(): RequestMatcher = RequestMatcher(
            path = path,
            method = method,
            matcher = request.matcher,
            contentMatcher = request.contentMatcher,
            requestContentType = request.contentType,
            requestContent = request.body,
            requestHeaders = request.headers,
            responseStatus = response.status,
            responseContentType = response.contentType,
            responseContent = response.bodyContent,
            expectedState = request.expectedState,
            setState = response.newState,
            responseHeaders = response.headers,
            responseException = response.exception,
            queryParams = request.queryParams
        )
        
        class RequestBuilder {
            internal var matcher: ((HttpRequestData) -> Boolean)? = null
            internal var contentType: ContentType? = null
            internal var body: ByteArray? = null
            internal var contentMatcher: ContentMatcher = DefaultContentMatcher
            internal var expectedState: String? = null
            internal var headers: MutableMap<String, String> = mutableMapOf()
            internal var queryParams: QueryParams? = null

            fun contentType(contentType: ContentType) {
                this.contentType = contentType
            }

            fun contentType(contentType: String) {
                this.contentType = ContentType.parse(contentType)
            }

            fun header(name: String, value: String) {
                headers[name] = value
            }

            fun matching(matcher: (HttpRequestData) -> Boolean) {
                this.matcher = matcher
            }

            fun bodyFromResource(path: String) {
                body = this.javaClass.getResource(path)?.readBytes()
                    ?: error("Unable to load resource file '$path'")
            }

            fun body(content: String) {
                body = content.toByteArray(Charsets.UTF_8)
            }

            fun withContentMatcher(contentMatcher: ContentMatcher) {
                this.contentMatcher = contentMatcher
            }

            fun formBody(
                ignoreUnknownKeys: Boolean = false,
                builder: FormBodyBuilder.() -> Unit
            ) {
                val formBuilder = FormBodyBuilder().apply(builder)
                body = formBuilder.build().toByteArray(Charsets.UTF_8)
                contentMatcher = formBuilder.buildMatcher(ignoreUnknownKeys)
            }

            fun queryParams(
                ignoreUnknownParams: Boolean = false,
                builder: QueryParamsBuilder.() -> Unit
            ) {
                val paramsBuilder = QueryParamsBuilder().apply(builder)
                queryParams = paramsBuilder.build(ignoreUnknownParams)
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

        if (method != null && method != data.method)
            return Mismatch("Method mismatch: expected $method but was ${data.method}")

        if (path != null && path != data.url.encodedPath)
            return Mismatch("Path mismatch: expected $path but was ${data.url.encodedPath}")

        if (requestContentType != null && requestContentType != data.body.contentType)
            return Mismatch("Content-Type mismatch: expected $requestContentType but was ${data.body.contentType}")

        for (header in requestHeaders) {
            val actualValue = data.headers[header.key] ?: return Mismatch("Header mismatch: expected ${header.key}=${header.value} but was missing")
            if (actualValue != header.value) return Mismatch("Header mismatch: expected ${header.key}=${header.value} but was $actualValue")
        }

        if (queryParams != null) {
            val result = queryParams.matches(data.url.parameters)
            if (result is Mismatch) return result
        }

        if (matcher != null && !matcher(data))
            return Mismatch("Custom matcher failed")

        if (requestContent != null) {
            val body = (data.body as? OutgoingContent.ByteArrayContent)?.bytes() ?: return Mismatch("Body mismatch: request body is not available as ByteArray")
            return contentMatcher.matches(body, requestContent)
        }

        return Match
    }
    
}