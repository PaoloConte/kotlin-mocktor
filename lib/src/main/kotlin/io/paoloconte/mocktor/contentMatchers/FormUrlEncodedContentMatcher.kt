package io.paoloconte.mocktor.contentMatchers

import io.paoloconte.mocktor.MatchResult
import io.paoloconte.mocktor.valueMatchers.StringMatchable
import java.net.URLDecoder

/**
 * A content matcher for form-urlencoded request bodies.
 *
 * Example usage:
 * ```kotlin
 * request {
 *     formParams have "username" equalTo "john"
 *     formParams have "password" like ".*"
 *     formParams dontHave "admin"
 * }
 * ```
 */
class FormUrlEncodedContentMatcher: ContentMatcher {
    private val params: MutableMap<String, MutableList<StringMatchable>> = mutableMapOf()
    internal var ignoreUnknownKeys: Boolean = true

    /**
     * Specifies that the form body should have a parameter with the given name.
     *
     * @param name The parameter name.
     * @return A [StringMatchable] for defining the expected value.
     */
    infix fun have(name: String): StringMatchable {
        return StringMatchable()
            .also { params.getOrPut(name, { mutableListOf() }).add(it) }
    }

    /**
     * Specifies that the form body should not have a parameter with the given name.
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

    /** Returns true if no parameters are configured and unknown keys are ignored. */
    fun isEmpty() = params.isEmpty() && ignoreUnknownKeys

    /** Returns true if parameters are configured or unknown keys are not ignored. */
    fun isNotEmpty() = !isEmpty()

    override fun matches(body: ByteArray): MatchResult {
        val bodyParams = parseFormUrlEncoded(body.decodeToString())
            .getOrElse { return MatchResult.Mismatch("Unable to parse body: ${it.message}") }

        val missingInBody = params.keys - bodyParams.keys
        val extraInBody = bodyParams.keys - params.keys

        if (missingInBody.isNotEmpty() || (!ignoreUnknownKeys && extraInBody.isNotEmpty())) {
            val details = buildList {
                if (missingInBody.isNotEmpty()) add("missing: $missingInBody")
                if (!ignoreUnknownKeys && extraInBody.isNotEmpty()) add("extra: $extraInBody")
            }.joinToString(", ")
            return MatchResult.Mismatch("Form parameter keys mismatch: $details")
        }

        for ((key, targetValues)  in params) {
            val bodyValues = bodyParams[key]!!
            for (value in targetValues) {
                if (bodyValues.none { v -> value.matcher!!.matches(v) }) {
                    return MatchResult.Mismatch("Form parameter '$key' mismatch: expected ${value.matcher} but was $bodyValues")
                }
            }
        }

        return MatchResult.Match
    }

    private fun parseFormUrlEncoded(content: String): Result<Map<String, List<String>>> {
        return runCatching {
            if (content.isBlank()) {
                return@runCatching emptyMap()
            }
            content.split("&")
                .map { pair ->
                    val parts = pair.split("=", limit = 2)
                    val key = URLDecoder.decode(parts[0], Charsets.UTF_8)
                    val value = if (parts.size > 1) URLDecoder.decode(parts[1], Charsets.UTF_8) else ""
                    key to value
                }
                .groupBy({ it.first }, { it.second })
        }
    }
}
