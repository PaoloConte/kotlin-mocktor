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

    infix fun equalToResource(path: String) {
        val bytes = (this.javaClass.getResource(path)?.readBytes()
            ?: error("Unable to load resource file '$path'"))
        matcher = DefaultContentMatcher(bytes)
    }
}