package io.paoloconte.mocktor.contentMatchers

import io.paoloconte.mocktor.MatchResult
import io.paoloconte.mocktor.MatchResult.Match
import io.paoloconte.mocktor.MatchResult.Mismatch

class DefaultContentMatcher(val bytes: ByteArray): ContentMatcher {
    override fun matches(body: ByteArray): MatchResult {
        return if (body.contentEquals(bytes))
            Match
        else
            Mismatch("Byte content mismatch")
    }
}