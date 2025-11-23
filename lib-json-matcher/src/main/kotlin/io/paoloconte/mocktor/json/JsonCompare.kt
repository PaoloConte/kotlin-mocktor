package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.json.JsonCompareResult.Equal
import io.paoloconte.mocktor.json.JsonCompareResult.NotEqual
import kotlinx.serialization.json.*
import kotlinx.serialization.json.doubleOrNull
import kotlin.collections.count
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.intersect
import kotlin.collections.minus
import kotlin.collections.plus

internal sealed interface JsonCompareResult {
    data object Equal : JsonCompareResult
    data class NotEqual(val message: String) : JsonCompareResult
}

internal fun JsonElement.compareWith(expected: JsonElement?, path: String = "$", ignoreFields: Set<String> = emptySet()): JsonCompareResult {
    val equal = when (this) {
        is JsonArray -> {
            if (expected !is JsonArray)
                return NotEqual("Expecting an array at path $path")
            if (expected.size != this.size)
                return NotEqual("Arrays at $path have different sizes")
            this.forEachIndexed { i, v ->
                val match = v.compareWith(expected[i], "$path[$i]", ignoreFields)
                if (match !is Equal) {
                    return match
                }
            }
            true
        }

        is JsonObject -> {
            if (expected !is JsonObject)
                return NotEqual("Expecting an object at path $path")
            if (expected.count { it.value !is JsonNull } != this.count { it.value !is JsonNull })
                return NotEqual("Objects at $path have mismatched keys ${this.keys.plus(expected.keys).minus(this.keys.intersect(expected.keys))}")
            this.entries.forEach { (k, v) ->
                if (ignoreFields.contains(k)) return@forEach
                val match = v.compareWith(expected[k], "$path.$k", ignoreFields)
                if (match is NotEqual) {
                    return match
                }
            }
            true
        }

        is JsonNull -> expected is JsonNull || expected == null
        is JsonPrimitive -> this.compareWith(expected)
    }
    
    return if (equal)
        Equal
    else
        NotEqual("Expected $expected but was $this at path $path")
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