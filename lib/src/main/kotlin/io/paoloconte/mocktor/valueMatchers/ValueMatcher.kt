package io.paoloconte.mocktor.valueMatchers

interface ValueMatcher<T> {
    fun matches(other: T?): Boolean
}