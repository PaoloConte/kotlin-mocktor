package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.MatchResult
import io.paoloconte.mocktor.MatchResult.Match
import io.paoloconte.mocktor.MatchResult.Mismatch
import kotlinx.serialization.json.*

internal fun JsonElement.compareWith(expected: JsonElement?, path: String = "$", ignoreFields: Set<String> = emptySet()): MatchResult {
    val equal = when (this) {
        is JsonArray -> {
            if (expected !is JsonArray)
                return Mismatch("Expecting an array at path $path")
            if (expected.size != this.size)
                return Mismatch("Arrays at $path have different sizes")
            this.forEachIndexed { i, v ->
                val match = v.compareWith(expected[i], "$path[$i]", ignoreFields)
                if (match !is Match) {
                    return match
                }
            }
            true
        }

        is JsonObject -> {
            if (expected !is JsonObject)
                return Mismatch("Expecting an object at path $path")
            if (expected.count { it.value !is JsonNull } != this.count { it.value !is JsonNull })
                return Mismatch("Objects at $path have mismatched keys ${this.keys.plus(expected.keys).minus(this.keys.intersect(expected.keys))}")
            this.entries.forEach { (k, v) ->
                if (ignoreFields.contains(k)) return@forEach
                val match = v.compareWith(expected[k], "$path.$k", ignoreFields)
                if (match is Mismatch) {
                    return match
                }
            }
            true
        }

        is JsonNull -> expected is JsonNull || expected == null
        is JsonPrimitive -> this.compareWith(expected)
    }
    
    return if (equal)
        Match
    else
        Mismatch("Expected $expected but was $this at path $path")
}

private fun JsonPrimitive.compareWith(expected: JsonElement?): Boolean {
    if (expected !is JsonPrimitive)
        return false
    if (this.content == expected.content)
        return true
    if (this.doubleOrNull != null && this.doubleOrNull == expected.doubleOrNull)  // this is to compare double and BigDecimal or BigDecimals with different precision
        return true
    return false
}