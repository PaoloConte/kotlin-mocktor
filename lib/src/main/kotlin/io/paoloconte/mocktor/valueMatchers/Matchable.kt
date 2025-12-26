package io.paoloconte.mocktor.valueMatchers

import io.ktor.http.ContentType

open class Matchable<T>(val negate: Boolean = false) {
    internal var matcher: ValueMatcher<T>? = null

    infix fun equalTo(other: T) {
        matcher = EqualValueMatcher(other)
    }
    
    infix fun notEqualTo(other: T) {
        matcher = NotEqualValueMatcher(other)
    }

    internal fun ignore() {
        matcher = IgnoreValueMatcher()
    }
}

open class StringMatchable(negate: Boolean = false) : Matchable<String>(negate) {
    infix fun like(regex: String) {
        matcher = LikeValueMatcher(regex)
    }

    infix fun notLike(regex: String) {
        matcher = NotLikeValueMatcher(regex)
    }

    infix fun containing(other: String) {
        matcher = ContainsValueMatcher(other)
    }

    infix fun notContaining(other: String) {
        matcher = NotContainsValueMatcher(other)
    }

}

class ContentTypesMatchable(negate: Boolean = false) : StringMatchable(negate) {
    infix fun equalTo(other: ContentType) {
        matcher = EqualValueMatcher(other.toString())
    }
    
    infix fun notEqualTo(other: ContentType) {
        matcher = NotEqualValueMatcher(other.toString())
    }
}
