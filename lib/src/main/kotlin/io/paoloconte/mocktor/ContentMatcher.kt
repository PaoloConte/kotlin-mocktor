package io.paoloconte.mocktor

interface ContentMatcher {
    fun matches(body: ByteArray, target: ByteArray): MatchResult
}