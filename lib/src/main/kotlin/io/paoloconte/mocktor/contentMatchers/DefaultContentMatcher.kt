package io.paoloconte.mocktor.contentMatchers

import io.paoloconte.mocktor.MatchResult
import io.paoloconte.mocktor.MatchResult.Match
import io.paoloconte.mocktor.MatchResult.Mismatch

object DefaultContentMatcher: ContentMatcher {
    override fun matches(body: ByteArray, target: ByteArray): MatchResult {
        return if (body.contentEquals(target))
            Match
        else
            Mismatch("Byte content mismatch")
    }
}