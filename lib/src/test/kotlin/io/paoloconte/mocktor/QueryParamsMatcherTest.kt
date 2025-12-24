package io.paoloconte.mocktor

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
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
                queryParams {
                    param("page", "1")
                    param("limit", "10")
                }
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
                queryParams {
                    param("page", "1")
                    param("limit", "10")
                }
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
                queryParams {
                    param("page", "1")
                }
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
                queryParams {
                    param("page", "1")
                    param("limit", "10")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?page=1")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `does not match when extra query params present`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams {
                    param("page", "1")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?page=1&limit=10")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches with extra query params when ignoreUnknownParams is true`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams(ignoreUnknownParams = true) {
                    param("page", "1")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?page=1&limit=10&sort=name")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `still requires expected params when ignoreUnknownParams is true`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams(ignoreUnknownParams = true) {
                    param("page", "1")
                    param("limit", "10")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?page=1&extra=value")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches ignoring specific params`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams {
                    param("page", "1")
                    param("timestamp", "123")
                    ignoreParam("timestamp")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?page=1&timestamp=999")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches ignoring multiple params`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams {
                    param("page", "1")
                    param("timestamp", "123")
                    param("nonce", "abc")
                    ignoreParams("timestamp", "nonce")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?page=1&timestamp=999&nonce=xyz")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches with both ignoreParam and ignoreUnknownParams`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams(ignoreUnknownParams = true) {
                    param("page", "1")
                    param("timestamp", "123")
                    ignoreParam("timestamp")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?page=1&timestamp=999&extra=ignored")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches multi-value query params`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams {
                    param("tag", "kotlin")
                    param("tag", "java")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?tag=kotlin&tag=java")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `does not match when multi-value param order differs`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams {
                    param("tag", "kotlin")
                    param("tag", "java")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users?tag=java&tag=kotlin")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches empty query params`() = runTest {
        MockEngine.get("/api/users") {
            request {
                queryParams { }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/users")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `matches query params with URL encoded values`() = runTest {
        MockEngine.get("/api/search") {
            request {
                queryParams {
                    param("q", "hello world")
                }
            }
            response {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.get("http://localhost/api/search?q=hello%20world")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}