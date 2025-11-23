package io.paoloconte.mocktor

object DefaultContentMatcher: ContentMatcher {
    override fun matches(body: ByteArray, target: ByteArray): Boolean {
        return body.contentEquals(target)
    }
}