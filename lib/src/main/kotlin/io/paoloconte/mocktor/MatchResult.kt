package io.paoloconte.mocktor

/**
 * Represents the result of matching an HTTP request against a [RequestMatcher].
 */
sealed interface MatchResult {
    /**
     * Indicates that the request matches all criteria.
     */
    data object Match : MatchResult

    /**
     * Indicates that the request does not match.
     *
     * @property reason A human-readable description of why the match failed.
     */
    data class Mismatch(val reason: String) : MatchResult
}
