package io.paoloconte.mocktor.xml

import io.paoloconte.mocktor.MatchResult
import kotlin.test.Test
import kotlin.test.assertIs

class XmlContentMatcherTest {

    private val matcher = XmlContentMatcher

    @Test
    fun `matches identical XML`() {
        val xml = "<root><item>value</item></root>"
        assertIs<MatchResult.Match>(matcher.matches(xml.toByteArray(), xml.toByteArray()))
    }

    @Test
    fun `matches XML with different whitespace`() {
        val body = "<root><item>value</item></root>"
        val target = """
            <root>
                <item>value</item>
            </root>
        """.trimIndent()
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `matches XML with different element order`() {
        val body = "<root><a>1</a><b>2</b></root>"
        val target = "<root><b>2</b><a>1</a></root>"
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `matches XML ignoring comments`() {
        val body = "<root><!-- comment --><item>value</item></root>"
        val target = "<root><item>value</item></root>"
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match XML with different values`() {
        val body = "<root><item>value1</item></root>"
        val target = "<root><item>value2</item></root>"
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match XML with different structure`() {
        val body = "<root><item>value</item></root>"
        val target = "<root><other>value</other></root>"
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match XML with missing elements`() {
        val body = "<root><a>1</a><b>2</b></root>"
        val target = "<root><a>1</a></root>"
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `returns false for invalid XML`() {
        val body = "not xml"
        val target = "<root></root>"
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `matches XML with attributes`() {
        val body = """<root attr="value"><item>text</item></root>"""
        val target = """<root attr="value"><item>text</item></root>"""
        assertIs<MatchResult.Match>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }

    @Test
    fun `does not match XML with different attributes`() {
        val body = """<root attr="value1"><item>text</item></root>"""
        val target = """<root attr="value2"><item>text</item></root>"""
        assertIs<MatchResult.Mismatch>(matcher.matches(body.toByteArray(), target.toByteArray()))
    }
}