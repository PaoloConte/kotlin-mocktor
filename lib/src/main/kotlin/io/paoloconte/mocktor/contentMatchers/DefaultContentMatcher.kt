package io.paoloconte.mocktor.contentMatchers

import io.paoloconte.mocktor.MatchResult
import io.paoloconte.mocktor.MatchResult.Match
import io.paoloconte.mocktor.MatchResult.Mismatch

/**
 * A content matcher that performs exact byte-by-byte comparison.
 *
 * @property bytes The expected byte array to match against.
 */
class DefaultContentMatcher(val bytes: ByteArray): ContentMatcher {
    /**
     * Checks if the body content exactly matches the expected bytes.
     *
     * @param body The request body to match.
     * @return [Match] if bytes are identical, [Mismatch] otherwise.
     */
    override fun matches(body: ByteArray): MatchResult {
        return if (body.contentEquals(bytes))
            Match
        else
            Mismatch("Byte content mismatch")
    }
}