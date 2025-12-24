package io.paoloconte.mocktor

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VerificationTest {

    private val client = HttpClient(MockEngineFactory)

    @AfterTest
    fun tearDown() {
        MockEngine.clear()
    }

    @Test
    fun `verify count matches exact number of requests`() = runTest {
        client.post("http://localhost/api/bookings")
        client.post("http://localhost/api/bookings")

        MockEngine.verify(count = 2) {
            method(HttpMethod.Post)
            path("/api/bookings")
        }
    }

    @Test
    fun `verify fails when count does not match`() = runTest {
        client.post("http://localhost/api/bookings")

        assertFailsWith<AssertionError> {
            MockEngine.verify(count = 2) {
                method(HttpMethod.Post)
                path("/api/bookings")
            }
        }
    }

    @Test
    fun `verify by method only`() = runTest {
        client.get("http://localhost/api/users")
        client.post("http://localhost/api/users")
        client.post("http://localhost/api/users")

        MockEngine.verify(count = 2) {
            method(HttpMethod.Post)
        }
    }

    @Test
    fun `verify by path only`() = runTest {
        client.get("http://localhost/api/users")
        client.get("http://localhost/api/users")
        client.get("http://localhost/api/orders")

        MockEngine.verify(count = 2) {
            path("/api/users")
        }
    }

    @Test
    fun `verify without count checks at least one request exists`() = runTest {
        client.post("http://localhost/api/bookings")

        MockEngine.verify {
            method(HttpMethod.Post)
            path("/api/bookings")
        }
    }

    @Test
    fun `verify fails when no matching request exists`() = runTest {
        client.get("http://localhost/api/users")

        assertFailsWith<AssertionError> {
            MockEngine.verify {
                method(HttpMethod.Post)
                path("/api/bookings")
            }
        }
    }

    @Test
    fun `verify with header matching`() = runTest {
        client.get("http://localhost/api/users") {
            header("Authorization", "Bearer token123")
        }
        client.get("http://localhost/api/users") {
            header("Authorization", "Bearer other")
        }

        MockEngine.verify(count = 1) {
            path("/api/users")
            header("Authorization", "Bearer token123")
        }
    }

    @Test
    fun `verify with query params`() = runTest {
        client.get("http://localhost/api/users?page=1&limit=10")
        client.get("http://localhost/api/users?page=2&limit=10")

        MockEngine.verify(count = 1) {
            path("/api/users")
            queryParams {
                param("page", "1")
                param("limit", "10")
            }
        }
    }

    @Test
    fun `verify count zero`() = runTest {
        client.get("http://localhost/api/users")

        MockEngine.verify(count = 0) {
            method(HttpMethod.Post)
            path("/api/users")
        }
    }

    @Test
    fun `requests returns all recorded requests`() = runTest {
        client.get("http://localhost/api/users?page=1")
        client.post("http://localhost/api/users")

        val requests = MockEngine.requests()
        assertEquals(2, requests.size)

        assertEquals(HttpMethod.Get, requests[0].method)
        assertEquals("/api/users", requests[0].url.encodedPath)
        assertEquals("1", requests[0].url.parameters["page"])

        assertEquals(HttpMethod.Post, requests[1].method)
        assertEquals("/api/users", requests[1].url.encodedPath)
    }

    @Test
    fun `clear resets recorded requests`() = runTest {
        client.get("http://localhost/api/users")
        assertEquals(1, MockEngine.requests().size)

        MockEngine.clear()
        assertEquals(0, MockEngine.requests().size)
    }
}