package io.paoloconte.mocktor.valueMatchers

class NotContainsValueMatcher(val value: String): ValueMatcher<String> {
    override fun matches(other: String?): Boolean {
        other ?: return false
        return !other.contains(value)
    }

    override fun toString(): String {
        return "not contains \"$value\""
    }
}