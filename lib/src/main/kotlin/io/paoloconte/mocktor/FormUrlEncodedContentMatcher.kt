package io.paoloconte.mocktor

import java.net.URLDecoder
import java.net.URLEncoder

class FormBodyBuilder {
    private val params = mutableListOf<Pair<String, String>>()
    private val ignoreFields = mutableSetOf<String>()

    fun param(key: String, value: String) {
        params.add(key to value)
    }

    fun ignoreField(field: String) {
        ignoreFields.add(field)
    }

    fun ignoreFields(vararg fields: String) {
        ignoreFields.addAll(fields)
    }

    internal fun build(): String {
        return params.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, Charsets.UTF_8)}=${URLEncoder.encode(value, Charsets.UTF_8)}"
        }
    }

    internal fun buildMatcher(ignoreUnknownKeys: Boolean): FormUrlEncodedContentMatcher {
        return FormUrlEncodedContentMatcher(ignoreFields, ignoreUnknownKeys)
    }
}

class FormUrlEncodedContentMatcher(
    private val ignoreFields: Set<String> = emptySet(),
    private val ignoreUnknownKeys: Boolean = false
) : ContentMatcher {

    override fun matches(body: ByteArray, target: ByteArray): MatchResult {
        val bodyParams = parseFormUrlEncoded(body.decodeToString())
            .getOrElse { return MatchResult.Mismatch("Unable to parse body: ${it.message}") }
        val targetParams = parseFormUrlEncoded(target.decodeToString())
            .getOrElse { return MatchResult.Mismatch("Unable to parse target: ${it.message}") }

        val filteredBody = bodyParams.filterKeys { it !in ignoreFields }
        val filteredTarget = targetParams.filterKeys { it !in ignoreFields }

        val missingInBody = filteredTarget.keys - filteredBody.keys
        val extraInBody = filteredBody.keys - filteredTarget.keys

        if (missingInBody.isNotEmpty() || (!ignoreUnknownKeys && extraInBody.isNotEmpty())) {
            val details = buildList {
                if (missingInBody.isNotEmpty()) add("missing: $missingInBody")
                if (!ignoreUnknownKeys && extraInBody.isNotEmpty()) add("extra: $extraInBody")
            }.joinToString(", ")
            return MatchResult.Mismatch("Form parameter keys mismatch: $details")
        }

        for (key in filteredTarget.keys) {
            val bodyValues = filteredBody[key]!!
            val targetValues = filteredTarget[key]!!
            if (bodyValues != targetValues) {
                return MatchResult.Mismatch("Form parameter '$key' mismatch: expected $targetValues but was $bodyValues")
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
