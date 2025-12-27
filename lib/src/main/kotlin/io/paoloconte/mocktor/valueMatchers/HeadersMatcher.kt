package io.paoloconte.mocktor.valueMatchers

import io.ktor.http.*
import io.paoloconte.mocktor.MatchResult

/**
 * A matcher for HTTP request headers.
 *
 * Example usage:
 * ```kotlin
 * request {
 *     headers have "Authorization" equalTo "Bearer token"
 *     headers have "Content-Type" like "application/json.*"
 *     headers dontHave "X-Debug"
 * }
 * ```
 */
class HeadersMatcher(
    private val params: MutableMap<String, StringMatchable> = mutableMapOf(),
) {

    /**
     * Specifies that the request should have a header with the given name.
     *
     * @param name The header name.
     * @return A [StringMatchable] for defining the expected value.
     */
    infix fun have(name: String): StringMatchable {
        return StringMatchable()
            .also { params[name] = it }
    }

    /**
     * Specifies that the request should not have a header with the given name.
     *
     * @param name The header name that should be absent.
     * @return A [StringMatchable] for additional constraints.
     */
    infix fun dontHave(name: String): StringMatchable {
        return StringMatchable(negate = true)
            .also { params[name] = it }
    }

    /** Returns true if no header matchers are configured. */
    fun isEmpty() = params.isEmpty()

    /** Returns true if header matchers are configured. */
    fun isNotEmpty() = !isEmpty()

    /**
     * Checks if the given headers match all configured criteria.
     *
     * @param actualHeaders The actual HTTP headers to match.
     * @return [MatchResult.Match] if all headers match, or [MatchResult.Mismatch] with a reason.
     */
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