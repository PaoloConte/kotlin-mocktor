package io.paoloconte.mocktor.json

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonContentMatcherTest {

    private val matcher = JsonContentMatcher

    @Test
    fun `matches identical JSON objects`() {
        val json = """{"name": "test", "value": 123}"""
        assertTrue(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `matches JSON with different key order`() {
        val body = """{"name": "test", "value": 123}"""
        val target = """{"value": 123, "name": "test"}"""
        assertTrue(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `matches JSON with different whitespace`() {
        val body = """{"name":"test","value":123}"""
        val target = """{
            "name": "test",
            "value": 123
        }"""
        assertTrue(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `matches identical JSON arrays`() {
        val json = """[1, 2, 3]"""
        assertTrue(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `matches nested JSON objects`() {
        val json = """{"outer": {"inner": "value"}}"""
        assertTrue(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `matches JSON with null values`() {
        val json = """{"name": null}"""
        assertTrue(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `matches JSON with boolean values`() {
        val json = """{"enabled": true, "disabled": false}"""
        assertTrue(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `matches JSON with equivalent numeric values`() {
        val body = """{"value": 1.0}"""
        val target = """{"value": 1.00}"""
        assertTrue(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON with different string values`() {
        val body = """{"name": "test1"}"""
        val target = """{"name": "test2"}"""
        assertFalse(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON with different numeric values`() {
        val body = """{"value": 123}"""
        val target = """{"value": 456}"""
        assertFalse(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON with different keys`() {
        val body = """{"name": "test"}"""
        val target = """{"title": "test"}"""
        assertFalse(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON with missing keys`() {
        val body = """{"name": "test", "value": 123}"""
        val target = """{"name": "test"}"""
        assertFalse(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON with extra keys`() {
        val body = """{"name": "test"}"""
        val target = """{"name": "test", "value": 123}"""
        assertFalse(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON arrays with different order`() {
        val body = """[1, 2, 3]"""
        val target = """[3, 2, 1]"""
        assertFalse(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON arrays with different sizes`() {
        val body = """[1, 2, 3]"""
        val target = """[1, 2]"""
        assertFalse(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match object with array`() {
        val body = """{"name": "test"}"""
        val target = """["test"]"""
        assertFalse(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `returns false for invalid JSON body`() {
        val body = "not json"
        val target = """{"name": "test"}"""
        assertFalse(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `returns false for invalid JSON target`() {
        val body = """{"name": "test"}"""
        val target = "not json"
        assertFalse(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `matches complex nested structure`() {
        val json = """
            {
                "users": [
                    {"id": 1, "name": "Alice", "active": true},
                    {"id": 2, "name": "Bob", "active": false}
                ],
                "metadata": {
                    "total": 2,
                    "page": 1
                }
            }
        """.trimIndent()
        assertTrue(matcher.matches(json.toByteArray(), json.toByteArray()))
    }
}