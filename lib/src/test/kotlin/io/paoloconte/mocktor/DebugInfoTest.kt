package io.paoloconte.mocktor

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DebugInfoTest {

    private val client = HttpClient(MockEngineFactory)

    @AfterTest
    fun tearDown() {
        MockEngine.clear()
    }

    @Test
    fun `returns mismatch details when no handler matches`() = runTest {
        MockEngine.get("/api/users") {
            response { status(HttpStatusCode.OK) }
        }
        MockEngine.post("/api/posts") {
            response { status(HttpStatusCode.Created) }
        }

        val response = client.get("http://localhost/api/posts")
        
        assertEquals(HttpStatusCode.NotFound, response.status)
        val body = response.bodyAsText()
        println(body) // For debugging the test itself
        
        assertTrue(body.contains("No matching handler found"))
        assertTrue(body.contains("1. [GET /api/users] -> Path mismatch"))
        assertTrue(body.contains("2. [POST /api/posts] -> Method mismatch"))
    }
}
