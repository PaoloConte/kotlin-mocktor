package io.paoloconte.mocktor.valueMatchers

class IgnoreValueMatcher<T>: ValueMatcher<T> {
    override fun matches(other: T?): Boolean {
        return true
    }
}