package io.paoloconte.mocktor.contentMatchers

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.paoloconte.mocktor.MockEngine
import io.paoloconte.mocktor.MockEngineFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FormUrlEncodedContentMatcherTest {

    private val client = HttpClient(MockEngineFactory)

    @AfterTest
    fun tearDown() {
        MockEngine.clear()
    }

    @Test
    fun `matches form body with same parameters`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formParams have "username" equalTo "john"
                formParams have "password" equalTo  "secret"
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john&password=secret")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches form body with different parameter order`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formParams have "username" equalTo "john"
                formParams have "password" equalTo  "secret"
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("password=secret&username=john")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `does not match form body with different values`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formParams have "username" equalTo "john"
                formParams have "password" equalTo  "secret"
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john&password=wrong")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `does not match form body with missing parameters`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formParams have "username" equalTo "john"
                formParams have "password" equalTo  "secret"
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
    
    @Test
    fun `matches form body with extra parameters`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formParams have "username" equalTo "john"
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john&password=secret")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }
    
    @Test
    fun `does not match form body with extra parameters in strict mode`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formParams have "username" equalTo "john"
                strictFormParams()
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john&password=secret")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches form body with URL encoded values`() = runTest {
        MockEngine.post("/api/search") {
            request {
                formParams have "query" equalTo "hello world"
                formParams have "filter" equalTo  "a&b"
            }
        }

        val response = client.post("http://localhost/api/search") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("query=hello+world&filter=a%26b")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches form body ignoring specified fields`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formParams have "username" equalTo "john"
                formParams have "password" equalTo "secret"
                formParams ignore "timestamp"
                strictFormParams()
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john&password=secret&timestamp=999")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches form body with multiple values for same parameter`() = runTest {
        MockEngine.post("/api/tags") {
            request {
                formParams have "tag" equalTo "kotlin"
                formParams have "tag" equalTo "java"
                formParams have "tag" equalTo "scala"
            }
        }

        val response = client.post("http://localhost/api/tags") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("tag=kotlin&tag=java&tag=scala")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches empty form body`() = runTest {
        MockEngine.post("/api/ping") {
        }

        val response = client.post("http://localhost/api/ping") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches form body with empty value`() = runTest {
        MockEngine.post("/api/data") {
            request {
                formParams have "key" equalTo ""
            }
        }

        val response = client.post("http://localhost/api/data") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("key=")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches form body using submitForm`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formParams have "username" equalTo "john"
                formParams have "password" equalTo "secret"
            }
        }

        val response = client.submitForm(
            url = "http://localhost/api/login",
            formParameters = parameters {
                append("username", "john")
                append("password", "secret")
            }
        )
        assertEquals(HttpStatusCode.OK, response.status)
    }

   
}
