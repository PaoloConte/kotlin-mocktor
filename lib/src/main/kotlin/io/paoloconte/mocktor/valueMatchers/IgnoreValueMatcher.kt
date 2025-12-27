package io.paoloconte.mocktor.valueMatchers

/**
 * A matcher that always returns true, effectively ignoring the value.
 *
 * Used when a field should be present but its value doesn't matter.
 *
 * @param T The type of value (ignored).
 */
class IgnoreValueMatcher<T>: ValueMatcher<T> {
    override fun matches(other: T?): Boolean {
        return true
    }
}