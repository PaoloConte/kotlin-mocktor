package io.paoloconte.mocktor.valueMatchers

class EqualValueMatcher<T>(val value: T): ValueMatcher<T> {
    override fun matches(other: T?): Boolean {
        return value == other
    }

    override fun toString(): String {
        return value.toString()
    }
}