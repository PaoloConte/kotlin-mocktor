package io.paoloconte.mocktor.valueMatchers

import io.ktor.http.Parameters
import io.paoloconte.mocktor.MatchResult
import kotlin.collections.iterator

class QueryParamsMatcher(
    private val params: MutableMap<String, MutableList<StringMatchable>> = mutableMapOf(),
) {
    internal var ignoreUnknownParams: Boolean = true

    infix fun have(name: String): StringMatchable {
        return StringMatchable()
            .also { params.getOrPut(name, { mutableListOf() }).add(it) }
    }
    
    infix fun dontHave(name: String): StringMatchable {
        return StringMatchable(negate = true)
            .also { params.getOrPut(name, { mutableListOf() }).add(it) }
    }

    infix fun ignore(name: String) {
        params[name] = mutableListOf(StringMatchable().apply { ignore() })
    }
    
    fun isEmpty() = params.isEmpty() && ignoreUnknownParams
    fun isNotEmpty() = !isEmpty()

    fun matches(actualParams: Parameters): MatchResult {
        val actualMap = actualParams.entries()
            .associate { it.key to it.value }

        val missingParams = params.keys - actualMap.keys
        val extraParams = actualMap.keys - params.keys

        if (missingParams.isNotEmpty() || (!ignoreUnknownParams && extraParams.isNotEmpty())) {
            val details = buildList {
                if (missingParams.isNotEmpty()) add("missing: $missingParams")
                if (!ignoreUnknownParams && extraParams.isNotEmpty()) add("extra: $extraParams")
            }.joinToString(", ")
            return MatchResult.Mismatch("Query parameter keys mismatch: $details")
        }

        for ((key, targetValues) in params) {
            val actualValues = actualMap[key] ?: emptyList()
            for (value in targetValues) {
                if (actualValues.none { v -> value.matcher!!.matches(v) }) {
                    return MatchResult.Mismatch("Query parameter '$key' mismatch: expected ${value.matcher} but was $actualValues")
                }
            }
        }

        return MatchResult.Match
    }
}