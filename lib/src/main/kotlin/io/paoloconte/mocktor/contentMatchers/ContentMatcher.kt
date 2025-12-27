package io.paoloconte.mocktor.contentMatchers

import io.paoloconte.mocktor.MatchResult

/**
 * Interface for matching HTTP request body content.
 *
 * Implementations can provide custom matching logic for different content types
 * such as JSON, XML, or form-urlencoded data.
 */
interface ContentMatcher {
    /**
     * Checks if the given body content matches the expected content.
     *
     * @param body The request body as a byte array.
     * @return [MatchResult.Match] if the body matches, or [MatchResult.Mismatch] with a reason.
     */
    fun matches(body: ByteArray): MatchResult
}