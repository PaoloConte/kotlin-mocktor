package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.MatchResult
import kotlin.test.Test
import kotlin.test.assertIs

class JsonContentMatcherTest {


    @Test
    fun `matches identical JSON objects`() {
        val json = """{"name": "test", "value": 123}"""
        val matcher = JsonContentMatcher(json.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray(), ))
    }

    @Test
    fun `matches JSON with different key order`() {
        val body = """{"name": "test", "value": 123}"""
        val target = """{"value": 123, "name": "test"}"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `matches JSON with different whitespace`() {
        val body = """{"name":"test","value":123}"""
        val target = """{
            "name": "test",
            "value": 123
        }"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `matches identical JSON arrays`() {
        val json = """[1, 2, 3]"""
        val matcher = JsonContentMatcher(json.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray()))
    }

    @Test
    fun `matches nested JSON objects`() {
        val json = """{"outer": {"inner": "value"}}"""
        val matcher = JsonContentMatcher(json.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray()))
    }

    @Test
    fun `matches JSON with null values`() {
        val json = """{"name": null}"""
        val matcher = JsonContentMatcher(json.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray()))
    }

    @Test
    fun `matches JSON with boolean values`() {
        val json = """{"enabled": true, "disabled": false}"""
        val matcher = JsonContentMatcher(json.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray()))
    }

    @Test
    fun `matches JSON with equivalent numeric values`() {
        val body = """{"value": 1.0}"""
        val target = """{"value": 1.00}"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match JSON with different string values`() {
        val body = """{"name": "test1"}"""
        val target = """{"name": "test2"}"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match JSON with different numeric values`() {
        val body = """{"value": 123}"""
        val target = """{"value": 456}"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match JSON with different keys`() {
        val body = """{"name": "test"}"""
        val target = """{"title": "test"}"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match JSON with missing keys`() {
        val body = """{"name": "test", "value": 123}"""
        val target = """{"name": "test"}"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match JSON with extra keys`() {
        val body = """{"name": "test"}"""
        val target = """{"name": "test", "value": 123}"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match JSON arrays with different order`() {
        val body = """[1, 2, 3]"""
        val target = """[3, 2, 1]"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match JSON arrays with different sizes`() {
        val body = """[1, 2, 3]"""
        val target = """[1, 2]"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match object with array`() {
        val body = """{"name": "test"}"""
        val target = """["test"]"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `returns false for invalid JSON body`() {
        val body = "not json"
        val target = """{"name": "test"}"""
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `returns false for invalid JSON target`() {
        val body = """{"name": "test"}"""
        val target = "not json"
        val matcher = JsonContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
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
        val matcher = JsonContentMatcher(json.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(json.toByteArray()))
    }

    @Test
    fun `ignoreUnknownKeys allows extra keys in body`() {
        val body = """{"name": "test", "extra": "value", "another": 123}"""
        val target = """{"name": "test"}"""
        val matcherWithIgnore = JsonContentMatcher(target.toByteArray(), ignoreUnknownKeys = true)
        assertIs<MatchResult.Match>(matcherWithIgnore.matches(body.toByteArray()))
    }

    @Test
    fun `null values are considered as missing`() {
        val body = """{"name": "test"}"""
        val target = """{"name": "test", "extra": null}"""
        val matcherWithIgnore = JsonContentMatcher(target.toByteArray(), ignoreUnknownKeys = false)
        assertIs<MatchResult.Match>(matcherWithIgnore.matches(body.toByteArray()))
    }

    @Test
    fun `ignoreUnknownKeys still requires expected keys to be present`() {
        val body = """{"extra": "value"}"""
        val target = """{"name": "test"}"""
        val matcherWithIgnore = JsonContentMatcher(target.toByteArray(), ignoreUnknownKeys = false)
        assertIs<MatchResult.Mismatch>(matcherWithIgnore.matches(body.toByteArray()))
    }

    @Test
    fun `ignoreUnknownKeys works with nested objects`() {
        val body = """{"outer": {"inner": "value", "extra": 123}, "topExtra": true}"""
        val target = """{"outer": {"inner": "value"}}"""
        val matcherWithIgnore = JsonContentMatcher(target.toByteArray(), ignoreUnknownKeys = true)
        assertIs<MatchResult.Match>(matcherWithIgnore.matches(body.toByteArray()))
    }

    @Test
    fun `ignoreUnknownKeys combined with ignoreFields`() {
        val body = """{"name": "test", "timestamp": "2024-01-01", "extra": "value"}"""
        val target = """{"name": "test", "timestamp": "ignored"}"""
        val matcherWithBoth = JsonContentMatcher(target.toByteArray(), ignoreFields = setOf("timestamp"), ignoreUnknownKeys = true)
        assertIs<MatchResult.Match>(matcherWithBoth.matches(body.toByteArray()))
    }
}