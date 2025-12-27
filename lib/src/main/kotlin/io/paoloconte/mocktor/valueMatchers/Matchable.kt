package io.paoloconte.mocktor.valueMatchers

import io.ktor.http.ContentType

/**
 * Base class for building value matchers with a fluent DSL.
 *
 * Provides basic equality matching operations.
 *
 * @param T The type of value to match.
 * @property negate If true, the match result will be negated.
 */
open class Matchable<T>(val negate: Boolean = false) {
    internal var matcher: ValueMatcher<T>? = null

    /**
     * Matches values that are equal to the given value.
     *
     * @param other The value to match against.
     */
    infix fun equalTo(other: T) {
        matcher = EqualValueMatcher(other)
    }

    /**
     * Matches values that are not equal to the given value.
     *
     * @param other The value that should not match.
     */
    infix fun notEqualTo(other: T) {
        matcher = NotEqualValueMatcher(other)
    }

    internal fun ignore() {
        matcher = IgnoreValueMatcher()
    }
}

/**
 * A matchable for string values with additional string-specific operations.
 *
 * Provides regex matching and substring containment in addition to equality.
 *
 * @property negate If true, the match result will be negated.
 */
open class StringMatchable(negate: Boolean = false) : Matchable<String>(negate) {
    /**
     * Matches strings that match the given regular expression.
     *
     * @param regex The regular expression pattern.
     */
    infix fun like(regex: String) {
        matcher = LikeValueMatcher(regex)
    }

    /**
     * Matches strings that do not match the given regular expression.
     *
     * @param regex The regular expression pattern that should not match.
     */
    infix fun notLike(regex: String) {
        matcher = NotLikeValueMatcher(regex)
    }

    /**
     * Matches strings that contain the given substring.
     *
     * @param other The substring to search for.
     * @return A [ContainsValueMatcher] for additional configuration.
     */
    infix fun containing(other: String): ContainsValueMatcher {
        return ContainsValueMatcher(other)
            .also { matcher = it }
    }

    /**
     * Matches strings that do not contain the given substring.
     *
     * @param other The substring that should be absent.
     * @return A [NotContainsValueMatcher] for additional configuration.
     */
    infix fun notContaining(other: String): NotContainsValueMatcher {
        return NotContainsValueMatcher(other)
            .also { matcher = it }
    }

}

/**
 * A matchable for content type values.
 *
 * Extends [StringMatchable] with support for [ContentType] objects.
 *
 * @property negate If true, the match result will be negated.
 */
class ContentTypesMatchable(negate: Boolean = false) : StringMatchable(negate) {
    /**
     * Matches content types equal to the given [ContentType].
     *
     * @param other The content type to match.
     */
    infix fun equalTo(other: ContentType) {
        matcher = EqualValueMatcher(other.toString())
    }

    /**
     * Matches content types not equal to the given [ContentType].
     *
     * @param other The content type that should not match.
     */
    infix fun notEqualTo(other: ContentType) {
        matcher = NotEqualValueMatcher(other.toString())
    }
}
