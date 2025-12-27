package io.paoloconte.mocktor.xml

import io.paoloconte.mocktor.RequestMatcher
import io.paoloconte.mocktor.valueMatchers.BodyMatchable

/**
 * Matches XML bodies from a classpath resource file.
 *
 * Example:
 * ```kotlin
 * request {
 *     body equalToXmlResource "/expected.xml"
 * }
 * ```
 *
 * @param path The resource path to the expected XML file.
 * @return An [XmlContentMatcher] for the comparison.
 */
context(builder: RequestMatcher.Builder.RequestBuilder)
infix fun BodyMatchable.equalToXmlResource(path: String): XmlContentMatcher {
     val bytes = (this.javaClass.getResource(path)?.readBytes()
                    ?: error("Unable to load resource file '$path'"))
    val contentMatcher = XmlContentMatcher(bytes)
    builder.withBodyMatcher(contentMatcher)
    return contentMatcher
}

/**
 * Matches XML bodies against the given XML string.
 *
 * Performs semantic comparison using XMLUnit, ignoring whitespace and comments.
 *
 * Example:
 * ```kotlin
 * request {
 *     body equalToXml "<root><item>value</item></root>"
 * }
 * ```
 *
 * @param content The expected XML content as a string.
 * @return An [XmlContentMatcher] for the comparison.
 */
context(builder: RequestMatcher.Builder.RequestBuilder)
infix fun BodyMatchable.equalToXml(content: String): XmlContentMatcher {
    val contentMatcher = XmlContentMatcher(content.toByteArray())
    builder.withBodyMatcher(contentMatcher)
    return contentMatcher
}
