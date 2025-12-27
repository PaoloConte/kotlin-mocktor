package io.paoloconte.mocktor.valueMatchers

import io.paoloconte.mocktor.contentMatchers.ContentMatcher
import io.paoloconte.mocktor.contentMatchers.DefaultContentMatcher

/**
 * A matchable for HTTP request body content.
 *
 * Provides various ways to match the request body including exact matching,
 * substring containment, and regex patterns.
 *
 * Example:
 * ```kotlin
 * request {
 *     body equalTo """{"name": "test"}"""
 *     // or
 *     body containing "test"
 *     // or
 *     body like ".*test.*"
 * }
 * ```
 */
class BodyMatchable {
    internal var matcher: ContentMatcher? = null

    /**
     * Matches bodies that exactly equal the given string content.
     *
     * @param content The expected body content as a UTF-8 string.
     */
    infix fun equalTo(content: String) {
        matcher = DefaultContentMatcher(content.toByteArray(Charsets.UTF_8))
    }

    /**
     * Matches bodies that exactly equal the given byte array.
     *
     * @param content The expected body content as bytes.
     */
    infix fun equalTo(content: ByteArray) {
        matcher = DefaultContentMatcher(content)
    }

    /**
     * Matches bodies that contain the given substring.
     *
     * @param substring The substring to search for.
     * @return A [ContainsValueMatcher] for additional configuration.
     */
    infix fun containing(substring: String): ContainsValueMatcher {
        return ContainsValueMatcher(substring)
            .also { matcher = it }
    }

    /**
     * Matches bodies that do not contain the given substring.
     *
     * @param other The substring that should be absent.
     * @return A [NotContainsValueMatcher] for additional configuration.
     */
    infix fun notContaining(other: String): NotContainsValueMatcher {
        return NotContainsValueMatcher(other)
            .also { matcher = it }
    }

    /**
     * Matches bodies that match the given regular expression.
     *
     * @param regex The regular expression pattern.
     */
    infix fun like(regex: String) {
        matcher = LikeValueMatcher(regex)
    }

    /**
     * Matches bodies that do not match the given regular expression.
     *
     * @param regex The regular expression pattern that should not match.
     */
    infix fun notLike(regex: String) {
        matcher = NotLikeValueMatcher(regex)
    }

    /**
     * Matches bodies that exactly equal the content of a classpath resource.
     *
     * @param path The resource path (e.g., "/expected-body.json").
     */
    infix fun equalToResource(path: String) {
        val bytes = (this.javaClass.getResource(path)?.readBytes()
            ?: error("Unable to load resource file '$path'"))
        matcher = DefaultContentMatcher(bytes)
    }
}