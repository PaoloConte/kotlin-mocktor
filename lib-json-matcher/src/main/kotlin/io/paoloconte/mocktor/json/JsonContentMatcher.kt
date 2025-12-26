package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.contentMatchers.ContentMatcher
import io.paoloconte.mocktor.MatchResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class JsonContentMatcher(
    private val target: ByteArray,
    var ignoreFields: Set<String> = emptySet(),
    var ignoreUnknownKeys: Boolean = false
): ContentMatcher {
    
    infix fun ignoreFields(fields: Set<String>): JsonContentMatcher {
        ignoreFields = fields
        return this
    }
    
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
