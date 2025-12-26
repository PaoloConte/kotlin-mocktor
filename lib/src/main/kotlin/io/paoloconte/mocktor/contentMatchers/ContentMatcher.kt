package io.paoloconte.mocktor.contentMatchers

import io.paoloconte.mocktor.MatchResult

interface ContentMatcher {
    fun matches(body: ByteArray): MatchResult
}