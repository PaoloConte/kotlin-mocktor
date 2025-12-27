package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.RequestMatcher
import io.paoloconte.mocktor.valueMatchers.BodyMatchable
import kotlin.String

/**
 * Matches JSON bodies from a classpath resource file.
 *
 * Example:
 * ```kotlin
 * request {
 *     body equalToJsonResource "/expected.json"
 * }
 * ```
 *
 * @param path The resource path to the expected JSON file.
 * @return A [JsonContentMatcher] for additional configuration.
 */
context(builder: RequestMatcher.Builder.RequestBuilder)
infix fun BodyMatchable.equalToJsonResource(
    path: String,
): JsonContentMatcher {
    val bytes = (this.javaClass.getResource(path)?.readBytes()
                    ?: error("Unable to load resource file '$path'"))
    val contentMatcher = JsonContentMatcher(bytes)
    builder.withBodyMatcher(contentMatcher)
    return contentMatcher
}

/**
 * Matches JSON bodies against the given JSON string.
 *
 * Performs semantic comparison, ignoring formatting differences.
 *
 * Example:
 * ```kotlin
 * request {
 *     body equalToJson """{"name": "test"}"""
 * }
 * ```
 *
 * @param content The expected JSON content as a string.
 * @return A [JsonContentMatcher] for additional configuration.
 */
context(builder: RequestMatcher.Builder.RequestBuilder)
infix fun BodyMatchable.equalToJson(
    content: String
): JsonContentMatcher {
    val contentMatcher = JsonContentMatcher(content.toByteArray())
    builder.withBodyMatcher(contentMatcher)
    return contentMatcher
}