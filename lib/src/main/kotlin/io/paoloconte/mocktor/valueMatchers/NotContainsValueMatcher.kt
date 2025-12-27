package io.paoloconte.mocktor.valueMatchers

import io.paoloconte.mocktor.MatchResult
import io.paoloconte.mocktor.MatchResult.Match
import io.paoloconte.mocktor.MatchResult.Mismatch
import io.paoloconte.mocktor.contentMatchers.ContentMatcher

/**
 * A matcher that checks if a string does not contain a substring.
 *
 * Also implements [ContentMatcher] for matching request body content.
 *
 * @property value The substring that should be absent.
 */
class NotContainsValueMatcher(val value: String) : ValueMatcher<String>, ContentMatcher {
    private var ignoreCase: Boolean = false

    override fun matches(other: String?): Boolean {
        other ?: return false
        return !other.contains(value, ignoreCase)
    }

    override fun toString(): String {
        return "not contains \"$value\""
    }

    override fun matches(body: ByteArray): MatchResult {
        return if (matches(body.decodeToString()))
            Match
        else
            Mismatch("Body does contain \"$value\"")
    }
}