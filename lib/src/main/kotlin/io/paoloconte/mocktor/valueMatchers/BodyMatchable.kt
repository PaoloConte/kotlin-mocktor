package io.paoloconte.mocktor.valueMatchers

import io.paoloconte.mocktor.contentMatchers.ContentMatcher
import io.paoloconte.mocktor.contentMatchers.DefaultContentMatcher

class BodyMatchable {
    internal var matcher: ContentMatcher? = null

    infix fun equalTo(content: String) {
        matcher = DefaultContentMatcher(content.toByteArray(Charsets.UTF_8))
    }

    infix fun equalTo(content: ByteArray) {
        matcher = DefaultContentMatcher(content)
    }

    infix fun containing(substring: String): ContainsValueMatcher {
        return ContainsValueMatcher(substring)
            .also { matcher = it }
    }

    infix fun notContaining(other: String): NotContainsValueMatcher {
        return NotContainsValueMatcher(other)
            .also { matcher = it }
    }

    infix fun like(regex: String) {
        matcher = LikeValueMatcher(regex)
    }

    infix fun notLike(regex: String) {
        matcher = NotLikeValueMatcher(regex)
    }

    infix fun equalToResource(path: String) {
        val bytes = (this.javaClass.getResource(path)?.readBytes()
            ?: error("Unable to load resource file '$path'"))
        matcher = DefaultContentMatcher(bytes)
    }
}