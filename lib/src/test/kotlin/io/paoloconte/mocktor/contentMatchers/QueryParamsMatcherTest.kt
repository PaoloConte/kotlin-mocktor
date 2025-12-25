package io.paoloconte.mocktor.contentMatchers

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.paoloconte.mocktor.MockEngine
import io.paoloconte.mocktor.MockEngineFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class QueryParamsMatcherTest {

    private val client = HttpClient(MockEngineFactory)

    @AfterTest
    fun tearDown() {
        MockEngine.clear()
    }

    @Test
    fun `matches query params`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams have "page" equalTo "1"
                queryParams have "limit" equalTo "10"
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?page=1&limit=10")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches query params in different order`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams have "page" equalTo "1"
                queryParams have "limit" equalTo "10"
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?limit=10&page=1")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `does not match when query param value differs`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams have "page" equalTo "1"
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?page=2")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `does not match when query param is missing`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams have "page" equalTo "1"
                queryParams have "limit" equalTo "10"
            }
        }

        val response = client.get("http://localhost/api/users?page=1")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `does not match when strict params and missing param`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams have "page" equalTo "1"
                strictQueryParams()
            }
        }

        val response = client.get("http://localhost/api/users?page=1&limit=10")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches when missing param as expected`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams dontHave "page" equalTo "1"
            }
        }

        val response = client.get("http://localhost/api/users?page=1&limit=10")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches when strict params and ignored param`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams have "page" equalTo "1"
                queryParams ignore "limit"
                strictQueryParams()
            }
        }

        val response = client.get("http://localhost/api/users?page=1&limit=10")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches multi-value query params`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams have "tag" equalTo "kotlin"
                queryParams have "tag" equalTo "java"
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?tag=kotlin&tag=java")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches query params with URL encoded values`() = runTest {
        MockEngine.get("/api/search") {
            request {
                queryParams have "q" equalTo "hello world"
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/search?q=hello%20world")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}