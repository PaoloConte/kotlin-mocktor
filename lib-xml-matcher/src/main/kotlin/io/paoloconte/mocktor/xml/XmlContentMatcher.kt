package io.paoloconte.mocktor.xml

import io.paoloconte.mocktor.ContentMatcher
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors

object XmlContentMatcher: ContentMatcher {
    override fun matches(body: ByteArray, target: ByteArray): Boolean {
        val diff = try {
            DiffBuilder.compare(body)
                .withTest(target)
                .ignoreWhitespace()
                .ignoreComments()
                .normalizeWhitespace()
                .withNodeMatcher(
                    DefaultNodeMatcher(
                        ElementSelectors.and(ElementSelectors.byNameAndText, ElementSelectors.byNameAndAllAttributes))
                )
                .checkForSimilar()
                .build()
                .hasDifferences()
        } catch (e: Exception) {
            return false
        }
        return !diff
    }

}