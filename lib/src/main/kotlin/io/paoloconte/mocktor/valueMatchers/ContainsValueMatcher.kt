package io.paoloconte.mocktor.valueMatchers

import io.paoloconte.mocktor.MatchResult
import io.paoloconte.mocktor.MatchResult.Match
import io.paoloconte.mocktor.MatchResult.Mismatch
import io.paoloconte.mocktor.contentMatchers.ContentMatcher

class ContainsValueMatcher(val value: String): ValueMatcher<String>, ContentMatcher {
    private var ignoreCase: Boolean = false
    
    override fun matches(other: String?): Boolean {
        other ?: return false
        return other.contains(value, ignoreCase)
    }

    override fun toString(): String {
        return "contains \"$value\""
    }
    
    override fun matches(body: ByteArray): MatchResult {
        return if (matches(body.decodeToString()))
            Match
        else
            Mismatch("Body does not contain \"$value\"")
    }
    
    infix fun ignoreCase(ignore: Boolean): ContainsValueMatcher {
        ignoreCase = ignore
        return this
    }
}