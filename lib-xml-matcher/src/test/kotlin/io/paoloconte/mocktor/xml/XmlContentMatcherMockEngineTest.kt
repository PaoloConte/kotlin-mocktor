package io.paoloconte.mocktor.xml

import io.paoloconte.mocktor.MockEngine
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.paoloconte.mocktor.MockEngineFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlContentMatcherMockEngineTest {

    private val client = HttpClient(MockEngineFactory)

    @AfterTest
    fun tearDown() {
        MockEngine.clear()
    }

    @Test
    fun `matches XML body with different whitespace`() = runTest {
        MockEngine.post("/api/data") {
            xmlBody("<root><item>value</item></root>")
            respond {
                status(HttpStatusCode.Created)
            }
        }

        val response = client.post("http://localhost/api/data") {
            contentType(ContentType.Application.Xml)
            setBody("""
                <root>
                    <item>value</item>
                </root>
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `does not match XML body with different values`() = runTest {
        MockEngine.post("/api/data") {
            xmlBody("<root><item>expected</item></root>")
            respond {
                status(HttpStatusCode.OK)
            }
        }

        val response = client.post("http://localhost/api/data") {
            contentType(ContentType.Application.Xml)
            setBody("<root><item>different</item></root>")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `matches XML body from resource file with different whitespace`() = runTest {
        MockEngine.post("/api/users") {
            xmlBodyFromResource("/fixtures/request.xml")
            respond {
                status(HttpStatusCode.Created)
            }
        }

        val response = client.post("http://localhost/api/users") {
            contentType(ContentType.Application.Xml)
            setBody("""
                <user>
                    <name>John</name>
                    <age>30</age>
                </user>
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }
}