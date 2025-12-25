package io.paoloconte.mocktor.valueMatchers

class NotLikeValueMatcher(val regex: String): ValueMatcher<String> {
    override fun matches(other: String?): Boolean {
        other ?: return false
        return !other.matches(regex.toRegex())
    }

    override fun toString(): String {
        return "not like \"$regex\""
    }
}