package io.paoloconte.mocktor

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
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
                formBody {
                    param("username", "john")
                    param("password", "secret")
                }
            }
            response {
                status(HttpStatusCode.OK)
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
                formBody {
                    param("username", "john")
                    param("password", "secret")
                }
            }
            response {
                status(HttpStatusCode.OK)
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
                formBody {
                    param("username", "john")
                    param("password", "secret")
                }
            }
            response {
                status(HttpStatusCode.OK)
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
                formBody {
                    param("username", "john")
                    param("password", "secret")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `does not match form body with extra parameters`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formBody {
                    param("username", "john")
                }
            }
            response {
                status(HttpStatusCode.OK)
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
                formBody {
                    param("query", "hello world")
                    param("filter", "a&b")
                }
            }
            response {
                status(HttpStatusCode.OK)
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
                formBody {
                    param("username", "john")
                    param("password", "secret")
                    param("timestamp", "123")
                    ignoreField("timestamp")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john&password=secret&timestamp=999")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches form body ignoring multiple fields`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formBody {
                    param("username", "john")
                    param("timestamp", "123")
                    param("nonce", "abc")
                    ignoreFields("timestamp", "nonce")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john&timestamp=999&nonce=xyz")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches form body with multiple values for same parameter`() = runTest {
        MockEngine.post("/api/tags") {
            request {
                formBody {
                    param("tag", "kotlin")
                    param("tag", "java")
                    param("tag", "scala")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/tags") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("tag=kotlin&tag=java&tag=scala")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `does not match when multi-value parameter order differs`() = runTest {
        MockEngine.post("/api/tags") {
            request {
                formBody {
                    param("tag", "kotlin")
                    param("tag", "java")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/tags") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("tag=java&tag=kotlin")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches empty form body`() = runTest {
        MockEngine.post("/api/ping") {
            request {
                formBody { }
            }
            response {
                status(HttpStatusCode.OK)
            }
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
                formBody {
                    param("key", "")
                }
            }
            response {
                status(HttpStatusCode.OK)
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
                formBody {
                    param("username", "john")
                    param("password", "secret")
                }
            }
            response {
                status(HttpStatusCode.OK)
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

    @Test
    fun `matches form body with extra parameters when ignoreUnknownKeys is true`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formBody(ignoreUnknownKeys = true) {
                    param("username", "john")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john&password=secret&extra=value")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `still requires all expected parameters when ignoreUnknownKeys is true`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formBody(ignoreUnknownKeys = true) {
                    param("username", "john")
                    param("password", "secret")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john&extra=value")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches with both ignoreFields and ignoreUnknownKeys`() = runTest {
        MockEngine.post("/api/login") {
            request {
                formBody(ignoreUnknownKeys = true) {
                    param("username", "john")
                    param("timestamp", "123")
                    ignoreField("timestamp")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=john&timestamp=999&extra=ignored")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
