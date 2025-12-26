package io.paoloconte.mocktor.xml

import io.paoloconte.mocktor.MatchResult
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertIs

class XmlContentMatcherTest {

    @Test
    fun `matches identical XML`() {
        val xml = "<root><item>value</item></root>"
        val matcher = XmlContentMatcher(xml.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(xml.toByteArray()))
    }

    @Test
    fun `matches XML with different whitespace`() {
        val body = "<root><item>value</item></root>"
        val target = """
            <root>
                <item>value</item>
            </root>
        """.trimIndent()
        val matcher = XmlContentMatcher(target.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `matches XML with different element order`() {
        val body = "<root><a>1</a><b>2</b></root>"
        val target = "<root><b>2</b><a>1</a></root>"
        val matcher = XmlContentMatcher(target.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `matches XML ignoring comments`() {
        val body = "<root><!-- comment --><item>value</item></root>"
        val target = "<root><item>value</item></root>"
        val matcher = XmlContentMatcher(target.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match XML with different values`() {
        val body = "<root><item>value1</item></root>"
        val target = "<root><item>value2</item></root>"
        val matcher = XmlContentMatcher(target.toByteArray())
        val result = matcher.matches(body.toByteArray())
        assertIs<MatchResult.Mismatch>(result)
        assertContains(result.reason, "Expected text value 'value1' but was 'value2'")
    }

    @Test
    fun `does not match XML with different structure`() {
        val body = "<root><item>value</item></root>"
        val target = "<root><other>value</other></root>"
        val matcher = XmlContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match XML with missing elements`() {
        val body = "<root><a>1</a><b>2</b></root>"
        val target = "<root><a>1</a></root>"
        val matcher = XmlContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `returns mismatch for invalid XML`() {
        val body = "not xml"
        val target = "<root></root>"
        val matcher = XmlContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `matches XML with attributes`() {
        val body = """<root><item attr="value">text</item></root>"""
        val target = """<root><item attr="value">text</item></root>"""
        val matcher = XmlContentMatcher(target.toByteArray())
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray()))
    }

    @Test
    fun `does not match XML with different attributes`() {
        val body = """<root><item attr="value1">text</item></root>"""
        val target = """<root><item attr="value2">text</item></root>"""
        val matcher = XmlContentMatcher(target.toByteArray())
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray()))
    }
}
