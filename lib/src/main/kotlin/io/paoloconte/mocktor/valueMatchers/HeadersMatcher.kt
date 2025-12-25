package io.paoloconte.mocktor.valueMatchers

import io.ktor.http.*
import io.paoloconte.mocktor.MatchResult

class HeadersMatcher(
    private val params: MutableMap<String, StringMatchable> = mutableMapOf(),
) {

    infix fun have(name: String): StringMatchable {
        return StringMatchable()
            .also { params[name] = it }
    }

    infix fun dontHave(name: String): StringMatchable {
        return StringMatchable(negate = true)
            .also { params[name] = it }
    }
    
    fun isEmpty() = params.isEmpty()
    fun isNotEmpty() = !isEmpty()

    fun matches(actualHeaders: Headers): MatchResult {
        for ((name, matchable) in params) {
            val actualValue = actualHeaders[name]
            if (!matchable.matcher!!.matches(actualValue).xor(matchable.negate)) {
                return MatchResult.Mismatch("Header '$name' mismatch: expected ${matchable.matcher} but was '$actualValue'")
            }
        }
        return MatchResult.Match
    }
}