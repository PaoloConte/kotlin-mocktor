package io.paoloconte.mocktor.valueMatchers

/**
 * A value matcher that checks for exact equality.
 *
 * @param T The type of value to match.
 * @property value The expected value.
 */
class EqualValueMatcher<T>(val value: T): ValueMatcher<T> {
    override fun matches(other: T?): Boolean {
        return value == other
    }

    override fun toString(): String {
        return value.toString()
    }
}