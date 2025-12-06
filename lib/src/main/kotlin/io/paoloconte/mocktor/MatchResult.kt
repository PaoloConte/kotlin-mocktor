package io.paoloconte.mocktor

sealed interface MatchResult {
    data object Match : MatchResult
    data class Mismatch(val reason: String) : MatchResult
}
