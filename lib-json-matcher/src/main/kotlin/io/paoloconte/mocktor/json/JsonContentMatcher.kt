package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.contentMatchers.ContentMatcher
import io.paoloconte.mocktor.MatchResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * A content matcher for JSON request bodies.
 *
 * Performs semantic JSON comparison, ignoring formatting differences.
 * Supports ignoring specific fields and unknown keys.
 *
 * Example usage:
 * ```kotlin
 * request {
 *     body equalToJson """{"name": "test"}""" ignoreFields setOf("timestamp")
 * }
 * ```
 *
 * @param target The expected JSON content as bytes.
 * @property ignoreFields Set of field names to ignore during comparison.
 * @property ignoreUnknownKeys If true, extra fields in the request body are ignored.
 */
class JsonContentMatcher(
    private val target: ByteArray,
    private var ignoreFields: Set<String> = emptySet(),
    private var ignoreUnknownKeys: Boolean = false
): ContentMatcher {

    /**
     * Specifies fields to ignore during JSON comparison.
     *
     * @param fields Set of field names to ignore.
     * @return This matcher for chaining.
     */
    infix fun ignoreFields(fields: Set<String>): JsonContentMatcher {
        ignoreFields = fields
        return this
    }

    /**
     * Configures whether extra fields in the request body should be ignored.
     *
     * @param ignore If true, unknown keys in the request are ignored.
     * @return This matcher for chaining.
     */
    infix fun ignoreUnknownKeys(ignore: Boolean): JsonContentMatcher {
        ignoreUnknownKeys = ignore
        return this
    }

    override fun matches(body: ByteArray): MatchResult {
        val jsonA = Json.parseToJsonElementOrNull(body.decodeToString())
            .getOrElse { return MatchResult.Mismatch("Unable to decode body: $it")  }
        val jsonB = Json.parseToJsonElementOrNull(target.decodeToString())
            .getOrElse { return MatchResult.Mismatch("Unable to decode target: $it")  }
        return jsonA.compareWith(jsonB, ignoreFields = ignoreFields, ignoreUnknownKeys = ignoreUnknownKeys)
    }

    private fun Json.parseToJsonElementOrNull(str: String): Result<JsonElement> {
        return runCatching {
            parseToJsonElement(str)
        }
    }
    
}
