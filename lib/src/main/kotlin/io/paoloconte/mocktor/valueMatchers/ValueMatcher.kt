package io.paoloconte.mocktor.valueMatchers

/**
 * Interface for matching values of type [T].
 *
 * Implementations provide different matching strategies such as equality,
 * regex matching, or substring containment.
 *
 * @param T The type of value to match.
 */
interface ValueMatcher<T> {
    /**
     * Checks if the given value matches this matcher's criteria.
     *
     * @param other The value to match, or null.
     * @return true if the value matches, false otherwise.
     */
    fun matches(other: T?): Boolean
}