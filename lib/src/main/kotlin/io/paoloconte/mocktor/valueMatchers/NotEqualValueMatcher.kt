package io.paoloconte.mocktor.valueMatchers

/**
 * A value matcher that checks for inequality.
 *
 * @param T The type of value to match.
 * @property value The value that should not match.
 */
class NotEqualValueMatcher<T>(val value: T): ValueMatcher<T> {
    override fun matches(other: T?): Boolean {
        return value != other
    }

    override fun toString(): String {
        return "not $value"
    }
}