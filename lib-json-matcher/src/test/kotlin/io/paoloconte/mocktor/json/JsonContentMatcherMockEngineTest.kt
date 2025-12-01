package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.MockEngine
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
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
            jsonBody("""{"name": "John", "age": 30}""")
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
            jsonBody("""{"name": "John", "age": 30}""")
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
            jsonBodyFromResource("/fixtures/request.json")
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
}