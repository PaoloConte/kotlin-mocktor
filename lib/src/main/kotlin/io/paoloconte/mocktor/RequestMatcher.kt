package io.paoloconte.mocktor

import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent

class RequestMatcher(
    val method: HttpMethod,
    val path: String,
    val matcher: ((HttpRequestData) -> Boolean)?,
    val requestContentType: ContentType?,
    val requestContent: ByteArray?,
    val contentMatcher: ContentMatcher,
    val responseStatus: HttpStatusCode,
    val responseContentType: ContentType,
    val responseContent: ((HttpRequestData) -> ByteArray)?,
) {
    class Builder(var method: HttpMethod, val path: String) {
        
        private val request = RequestBuilder()
        private val response = ResponseBuilder()
        
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
            responseStatus = response.status,
            responseContentType = response.contentType,
            responseContent = response.bodyContent,
        )
        
        class RequestBuilder {
            internal var matcher: ((HttpRequestData) -> Boolean)? = null
            internal var contentType: ContentType? = null
            internal var body: ByteArray? = null
            internal var contentMatcher: ContentMatcher = DefaultContentMatcher

            fun contentType(contentType: ContentType) {
                this.contentType = contentType
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
            
        }

        class ResponseBuilder {
            internal var status: HttpStatusCode = HttpStatusCode.OK
            internal var contentType: ContentType = ContentType.Application.Json
            internal var bodyContent: ((HttpRequestData) -> ByteArray)? = null
            
            fun status(status: HttpStatusCode) {
                this.status = status
            }
            
            fun contentType(contentType: ContentType) {
                this.contentType = contentType
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

    fun matches(data: HttpRequestData) : Boolean {
        if (method != data.method) return false
        if (path != data.url.encodedPath) return false
        if (requestContentType != null && requestContentType != data.body.contentType) return false
        if (matcher != null && !matcher(data)) return false
        if (requestContent != null) {
            val body = (data.body as? OutgoingContent.ByteArrayContent)?.bytes() ?: return false
            val match = contentMatcher.matches(body, requestContent)
            if (!match) return false
        }
        return true
    }
    
}