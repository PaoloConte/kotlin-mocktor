package io.paoloconte.mocktor.valueMatchers

class NotEqualValueMatcher<T>(val value: T): ValueMatcher<T> {
    override fun matches(other: T?): Boolean {
        return value != other
    }

    override fun toString(): String {
        return "not $value"
    }
}