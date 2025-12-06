package io.paoloconte.mocktor.xml

import io.paoloconte.mocktor.ContentMatcher
import io.paoloconte.mocktor.MatchResult
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors

object XmlContentMatcher: ContentMatcher {
    override fun matches(body: ByteArray, target: ByteArray): MatchResult {
        val diff = try {
            DiffBuilder.compare(body)
                .withTest(target)
                .ignoreWhitespace()
                .ignoreComments()
                .normalizeWhitespace()
                .withNodeMatcher(
                    DefaultNodeMatcher(ElementSelectors.byName)
                )
                .checkForSimilar()
                .build()
        } catch (e: Exception) {
            return MatchResult.Mismatch("XML parsing error: ${e.message}")
        }
        return if (!diff.hasDifferences())
            MatchResult.Match
        else
            MatchResult.Mismatch("XML content mismatch: $diff")
    }

}