package io.paoloconte.mocktor.valueMatchers

import io.ktor.http.Parameters
import io.paoloconte.mocktor.MatchResult
import kotlin.collections.iterator

/**
 * A matcher for URL query parameters.
 *
 * Example usage:
 * ```kotlin
 * request {
 *     queryParams have "page" equalTo "1"
 *     queryParams have "limit" like "\\d+"
 *     queryParams dontHave "debug"
 * }
 * ```
 */
class QueryParamsMatcher(
    private val params: MutableMap<String, MutableList<StringMatchable>> = mutableMapOf(),
) {
    internal var ignoreUnknownParams: Boolean = true

    /**
     * Specifies that the URL should have a query parameter with the given name.
     *
     * @param name The parameter name.
     * @return A [StringMatchable] for defining the expected value.
     */
    infix fun have(name: String): StringMatchable {
        return StringMatchable()
            .also { params.getOrPut(name, { mutableListOf() }).add(it) }
    }

    /**
     * Specifies that the URL should not have a query parameter with the given name.
     *
     * @param name The parameter name that should be absent.
     * @return A [StringMatchable] for additional constraints.
     */
    infix fun dontHave(name: String): StringMatchable {
        return StringMatchable(negate = true)
            .also { params.getOrPut(name, { mutableListOf() }).add(it) }
    }

    /**
     * Specifies that a parameter should be ignored during matching.
     *
     * @param name The parameter name to ignore.
     */
    infix fun ignore(name: String) {
        params[name] = mutableListOf(StringMatchable().apply { ignore() })
    }

    /** Returns true if no parameters are configured and unknown params are ignored. */
    fun isEmpty() = params.isEmpty() && ignoreUnknownParams

    /** Returns true if parameters are configured or unknown params are not ignored. */
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