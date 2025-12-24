package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.ContentMatcher
import io.paoloconte.mocktor.MatchResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class JsonContentMatcher(
    private val ignoreFields: Set<String> = emptySet()
): ContentMatcher {
    
    override fun matches(body: ByteArray, target: ByteArray): MatchResult {
        val jsonA = Json.parseToJsonElementOrNull(body.decodeToString())
            .getOrElse { return MatchResult.Mismatch("Unable to decode body: $it")  }
        val jsonB = Json.parseToJsonElementOrNull(target.decodeToString())
            .getOrElse { return MatchResult.Mismatch("Unable to decode target: $it")  }
        return jsonA.compareWith(jsonB, ignoreFields = ignoreFields)
    }

    private fun Json.parseToJsonElementOrNull(str: String): Result<JsonElement> {
        return runCatching {
            parseToJsonElement(str)
        }
    }
    
}
