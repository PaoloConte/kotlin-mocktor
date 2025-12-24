package io.paoloconte.mocktor

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MockEngineTest {

    private val client = HttpClient(MockEngineFactory)

    @AfterTest
    fun tearDown() {
        MockEngine.clear()
    }

    @Test
    fun `returns 404 when no handlers registered`() = runTest {
        val response = client.get("http://localhost/test")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches GET request by path`() = runTest {
        MockEngine.get("/api/users") {
            response {
                status(HttpStatusCode.OK)
                contentType("application/json")
                body("""{"users": []}""")
            }
        }

        val response = client.get("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Application.Json, response.contentType())
        assertEquals("""{"users": []}""", response.bodyAsText())
    }

    @Test
    fun `matches POST request`() = runTest {
        MockEngine.post("/api/users") {
            response {
                status(HttpStatusCode.Created)
                body("""{"id": 1}""")
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "test"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("""{"id": 1}""", response.bodyAsText())
    }

    @Test
    fun `matches POST request with content type`() = runTest {
        MockEngine.post("/api/users") {
            request {
                contentType(ContentType.Application.Json)
            }
            response {
                status(HttpStatusCode.Created)
                body("""{"id": 1}""")
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "test"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("""{"id": 1}""", response.bodyAsText())
    }

    @Test
    fun `matches POST request with content type as string`() = runTest {
        MockEngine.post("/api/users") {
            request {
                contentType("application/json")
            }
            response {
                status(HttpStatusCode.Created)
                body("""{"id": 1}""")
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "test"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("""{"id": 1}""", response.bodyAsText())
    }

    @Test
    fun `does not match when content type differs`() = runTest {
        MockEngine.post("/api/users") {
            request {
                contentType("text/xml")
            }
            response {
                status(HttpStatusCode.Created)
                body("""{"id": 1}""")
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "test"}""")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches PUT request`() = runTest {
        MockEngine.put("/api/users/1") {
            response {
                status(HttpStatusCode.OK)
                body("""{"updated": true}""")
            }
        }

        val response = client.put("http://localhost/api/users/1") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches DELETE request`() = runTest {
        MockEngine.delete("/api/users/1") {
            response {
                status(HttpStatusCode.NoContent)
            }
        }

        val response = client.delete("http://localhost/api/users/1") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `matches PATCH request`() = runTest {
        MockEngine.patch("/api/users/1") {
            response {
                status(HttpStatusCode.OK)
                body("""{"patched": true}""")
            }
        }

        val response = client.patch("http://localhost/api/users/1") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `returns 404 when path does not match`() = runTest {
        MockEngine.get("/api/users") {
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/other") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `returns 404 when method does not match`() = runTest {
        MockEngine.get("/api/users") {
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches request with custom matcher`() = runTest {
        MockEngine.get("/api/users") {
            request {
                matching { request ->
                    request.url.parameters["id"] == "123"
                }
            }
            response {
                status(HttpStatusCode.OK)
                body("""{"id": 123}""")
            }
        }

        val response = client.get("http://localhost/api/users?id=123") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `does not match when custom matcher fails`() = runTest {
        MockEngine.get("/api/users") {
            request {
                matching { request ->
                    request.url.parameters["id"] == "123"
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?id=456") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches request with body content`() = runTest {
        MockEngine.post("/api/users") {
            request {
                body("""{"name": "test"}""")
            }
            response {
                status(HttpStatusCode.Created)
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "test"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `does not match when body differs`() = runTest {
        MockEngine.post("/api/users") {
            request {
                body("""{"name": "test"}""")
            }
            response {
                status(HttpStatusCode.Created)
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "other"}""")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `clears handlers`() = runTest {
        MockEngine.get("/api/users") {
            response {
                status(HttpStatusCode.OK)
            }
        }

        MockEngine.clear()

        val response = client.get("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `first matching handler wins`() = runTest {
        MockEngine.get("/api/users") {
            response {
                status(HttpStatusCode.OK)
                body("first")
            }
        }
        MockEngine.get("/api/users") {
            response {
                status(HttpStatusCode.OK)
                body("second")
            }
        }

        val response = client.get("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
        }
        assertEquals("first", response.bodyAsText())
    }

    @Test
    fun `response has correct content type`() = runTest {
        MockEngine.get("/api/data") {
            response {
                status(HttpStatusCode.OK)
                contentType(ContentType.Application.Xml)
                body("<data/>")
            }
        }

        val response = client.get("http://localhost/api/data") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(ContentType.Application.Xml.toString(), response.contentType()?.toString())
    }

    @Test
    fun `matches request body containing specific json field using matching`() = runTest {
        MockEngine.post("/api/users") {
            request {
                matching { request ->
                    val body = request.bodyAsString()
                    body.contains("\"role\"") && body.contains("\"admin\"")
                }
            }
            response {
                status(HttpStatusCode.OK)
                body("""{"granted": true}""")
            }
        }

        // Should match - body contains "role" field with "admin" value
        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "John", "role": "admin"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"granted": true}""", response.bodyAsText())
    }

    @Test
    fun `does not match request body missing required json field using matching`() = runTest {
        MockEngine.post("/api/users") {
            request {
                matching { request ->
                    val body = request.bodyAsString()
                    body.contains("\"role\"") && body.contains("\"admin\"")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        // Should not match - body has "role" but not "admin"
        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "John", "role": "user"}""")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches request body from resource file`() = runTest {
        MockEngine.post("/api/users") {
            request {
                bodyFromResource("/fixtures/request.json")
            }
            response {
                status(HttpStatusCode.Created)
                bodyFromResource("/fixtures/response.json")
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "John", "age": 30}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("""{"id": 1, "created": true}""", response.bodyAsText())
    }
}