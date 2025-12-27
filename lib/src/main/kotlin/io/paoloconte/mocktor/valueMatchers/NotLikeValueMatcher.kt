package io.paoloconte.mocktor.valueMatchers

import io.paoloconte.mocktor.MatchResult
import io.paoloconte.mocktor.contentMatchers.ContentMatcher

/**
 * A matcher that checks if a string does not match a regular expression.
 *
 * Also implements [ContentMatcher] for matching request body content.
 *
 * @property regex The regular expression pattern that should not match.
 */
class NotLikeValueMatcher(val regex: String): ValueMatcher<String>, ContentMatcher {
    override fun matches(other: String?): Boolean {
        other ?: return false
        return !other.matches(regex.toRegex())
    }

    override fun toString(): String {
        return "not like \"$regex\""
    }

    override fun matches(body: ByteArray): MatchResult {
        return if (matches(body.decodeToString()))
            MatchResult.Match
        else
            MatchResult.Mismatch("Body does match regex \"$regex\"")
    }
}