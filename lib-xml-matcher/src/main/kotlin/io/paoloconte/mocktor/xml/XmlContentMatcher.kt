package io.paoloconte.mocktor.xml

import io.paoloconte.mocktor.contentMatchers.ContentMatcher
import io.paoloconte.mocktor.MatchResult
import org.slf4j.LoggerFactory
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors

class XmlContentMatcher(
    private val target: ByteArray,
): ContentMatcher {
    private val logger = LoggerFactory.getLogger(XmlContentMatcher::class.java)

    override fun matches(body: ByteArray): MatchResult {
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
            logger.trace("XML parsing error during matching")
            return MatchResult.Mismatch("XML parsing error: ${e.message}")
        }
        return if (!diff.hasDifferences()) {
            logger.trace("XML content matched successfully")
            MatchResult.Match
        } else {
            logger.trace("XML content mismatch: {}", diff)
            MatchResult.Mismatch("XML content mismatch: $diff")
        }
    }

}