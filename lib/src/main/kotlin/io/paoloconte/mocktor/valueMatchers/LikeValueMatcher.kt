package io.paoloconte.mocktor.valueMatchers

import io.paoloconte.mocktor.MatchResult
import io.paoloconte.mocktor.contentMatchers.ContentMatcher

class LikeValueMatcher(val regex: String): ValueMatcher<String>, ContentMatcher {
    override fun matches(other: String?): Boolean {
        other ?: return false
        return other.matches(regex.toRegex())
    }

    override fun toString(): String {
        return "like \"$regex\""
    }

    override fun matches(body: ByteArray): MatchResult {
        return if (matches(body.decodeToString()))
            MatchResult.Match
        else 
            MatchResult.Mismatch("Body does not match regex \"$regex\"")
    }
}