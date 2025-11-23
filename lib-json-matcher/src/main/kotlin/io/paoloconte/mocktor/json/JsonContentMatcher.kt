package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.ContentMatcher
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object JsonContentMatcher: ContentMatcher {
    
    override fun matches(body: ByteArray, target: ByteArray): Boolean {
        val jsonA = Json.parseToJsonElementOrNull(body.decodeToString())
        val jsonB = Json.parseToJsonElementOrNull(target.decodeToString())
        return jsonA?.compareWith(jsonB) == JsonCompareResult.Equal
    }

    private fun Json.parseToJsonElementOrNull(str: String?): JsonElement? {
        str ?: return null
        return try {
            parseToJsonElement(str)
        } catch (e: Exception) {
            null
        }
    }
    
}
