package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.MatchResult
import kotlin.test.Test
import kotlin.test.assertIs

class JsonContentMatcherTest {

    private val matcher = JsonContentMatcher()

    @Test
    fun `matches identical JSON objects`() {
        val json = """{"name": "test", "value": 123}"""
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `matches JSON with different key order`() {
        val body = """{"name": "test", "value": 123}"""
        val target = """{"value": 123, "name": "test"}"""
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `matches JSON with different whitespace`() {
        val body = """{"name":"test","value":123}"""
        val target = """{
            "name": "test",
            "value": 123
        }"""
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `matches identical JSON arrays`() {
        val json = """[1, 2, 3]"""
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `matches nested JSON objects`() {
        val json = """{"outer": {"inner": "value"}}"""
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `matches JSON with null values`() {
        val json = """{"name": null}"""
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `matches JSON with boolean values`() {
        val json = """{"enabled": true, "disabled": false}"""
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `matches JSON with equivalent numeric values`() {
        val body = """{"value": 1.0}"""
        val target = """{"value": 1.00}"""
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON with different string values`() {
        val body = """{"name": "test1"}"""
        val target = """{"name": "test2"}"""
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON with different numeric values`() {
        val body = """{"value": 123}"""
        val target = """{"value": 456}"""
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON with different keys`() {
        val body = """{"name": "test"}"""
        val target = """{"title": "test"}"""
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON with missing keys`() {
        val body = """{"name": "test", "value": 123}"""
        val target = """{"name": "test"}"""
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON with extra keys`() {
        val body = """{"name": "test"}"""
        val target = """{"name": "test", "value": 123}"""
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON arrays with different order`() {
        val body = """[1, 2, 3]"""
        val target = """[3, 2, 1]"""
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match JSON arrays with different sizes`() {
        val body = """[1, 2, 3]"""
        val target = """[1, 2]"""
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match object with array`() {
        val body = """{"name": "test"}"""
        val target = """["test"]"""
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `returns false for invalid JSON body`() {
        val body = "not json"
        val target = """{"name": "test"}"""
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `returns false for invalid JSON target`() {
        val body = """{"name": "test"}"""
        val target = "not json"
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
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
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray(), json.toByteArray()))
    }

    @Test
    fun `ignoreUnknownKeys allows extra keys in body`() {
        val matcherWithIgnore = JsonContentMatcher(ignoreUnknownKeys = true)
        val body = """{"name": "test", "extra": "value", "another": 123}"""
        val target = """{"name": "test"}"""
        assertIs<MatchResult.Match>(matcherWithIgnore.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `null values are considered as missing`() {
        val matcherWithIgnore = JsonContentMatcher(ignoreUnknownKeys = false)
        val body = """{"name": "test"}"""
        val target = """{"name": "test", "extra": null}"""
        assertIs<MatchResult.Match>(matcherWithIgnore.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `ignoreUnknownKeys still requires expected keys to be present`() {
        val matcherWithIgnore = JsonContentMatcher(ignoreUnknownKeys = true)
        val body = """{"extra": "value"}"""
        val target = """{"name": "test"}"""
        assertIs<MatchResult.Mismatch>(matcherWithIgnore.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `ignoreUnknownKeys works with nested objects`() {
        val matcherWithIgnore = JsonContentMatcher(ignoreUnknownKeys = true)
        val body = """{"outer": {"inner": "value", "extra": 123}, "topExtra": true}"""
        val target = """{"outer": {"inner": "value"}}"""
        assertIs<MatchResult.Match>(matcherWithIgnore.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `ignoreUnknownKeys combined with ignoreFields`() {
        val matcherWithBoth = JsonContentMatcher(ignoreFields = setOf("timestamp"), ignoreUnknownKeys = true)
        val body = """{"name": "test", "timestamp": "2024-01-01", "extra": "value"}"""
        val target = """{"name": "test", "timestamp": "ignored"}"""
        assertIs<MatchResult.Match>(matcherWithBoth.matches(body.toByteArray(), target.toByteArray()))
    }
}