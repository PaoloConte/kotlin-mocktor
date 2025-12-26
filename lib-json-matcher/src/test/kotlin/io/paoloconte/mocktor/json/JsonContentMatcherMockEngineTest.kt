package io.paoloconte.mocktor.json

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.paoloconte.mocktor.MockEngine
import io.paoloconte.mocktor.MockEngineFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonContentMatcherMockEngineTest {

    private val client = HttpClient(MockEngineFactory)

    @AfterTest
    fun tearDown() {
        MockEngine.clear()
    }

    @Test
    fun `matches JSON body with different key order`() = runTest {
        MockEngine.post("/api/users") {
            request {
                body equalToJson """{"name": "John", "age": 30}"""
            }
            response {
                status(HttpStatusCode.Created)
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"age": 30, "name": "John"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `does not match JSON body with different values`() = runTest {
        MockEngine.post("/api/users") {
            request {
                body equalToJson """{"name": "John", "age": 30}"""
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "John", "age": 25}""")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches JSON body from resource file with different key order`() = runTest {
        MockEngine.post("/api/users") {
            request {
                body equalToJsonResource "/fixtures/request.json"
            }
            response {
                status(HttpStatusCode.Created)
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"age": 30, "name": "John"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `matches JSON body ignoring multiple fields`() = runTest {
        MockEngine.post("/api/users") {
            request {
                val json = """{"name": "John", "age": 30, "createdAt": "2024-01-01", "updatedAt": "2024-01-02"}"""
                body equalToJson json ignoreFields setOf("createdAt", "updatedAt")
            }
            response {
                status(HttpStatusCode.Created)
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "John", "age": 30, "createdAt": "different", "updatedAt": "also different"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `does not match JSON body when non-ignored fields differ`() = runTest {
        MockEngine.post("/api/users") {
            request {
                val json = """{"name": "John", "age": 30, "timestamp": "2024-01-01"}"""
                body equalToJson json ignoreFields  setOf("timestamp")
            }
            response {
                status(HttpStatusCode.Created)
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Jane", "age": 30, "timestamp": "2024-01-01"}""")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}