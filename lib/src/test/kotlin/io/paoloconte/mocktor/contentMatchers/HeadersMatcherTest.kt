package io.paoloconte.mocktor.contentMatchers

import io.ktor.client.*
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.paoloconte.mocktor.MockEngine
import io.paoloconte.mocktor.MockEngineFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HeadersMatcherTest {

    private val client = HttpClient(MockEngineFactory)

    @AfterTest
    fun tearDown() {
        MockEngine.clear()
    }

    @Test
    fun `does not match when expected header is missing`() = runTest {
        MockEngine.get("/api/users") {
            request {
                headers have "Authorization" equalTo "Bearer token123"
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches when header is missing as expected`() = runTest {
        MockEngine.get("/api/users") {
            request {
                headers dontHave "Authorization" equalTo "Bearer token123"
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches when header has different as expected`() = runTest {
        MockEngine.get("/api/users") {
            request {
                headers dontHave "Authorization" equalTo "Bearer token123"
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users") {
            header("Authorization", "Bearer wrong-token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `does not match when header value differs`() = runTest {
        MockEngine.get("/api/users") {
            request {
                headers have "Authorization" equalTo "Bearer token123"
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users") {
            header("Authorization", "Bearer wrong-token")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches request with multiple expected headers`() = runTest {
        MockEngine.get("/api/users") {
            request {
                headers have "Authorization" equalTo "Bearer token123"
                headers have "X-Request-Id" equalTo "req-456"
            }
            response {
                status(HttpStatusCode.OK)
                body("""{"success": true}""")
            }
        }

        val response = client.get("http://localhost/api/users") {
            header("Authorization", "Bearer token123")
            header("X-Request-Id", "req-456")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches request with header not equal to`() = runTest {
        MockEngine.get("/api/users") {
            request {
                headers have "Authorization" equalTo "Bearer token123"
                headers have "X-Request-Id" notEqualTo "req-123"
            }
            response {
                status(HttpStatusCode.OK)
                body("""{"success": true}""")
            }
        }

        val response = client.get("http://localhost/api/users") {
            header("Authorization", "Bearer token123")
            header("X-Request-Id", "req-456")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }
}