package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.RequestMatcher
import io.paoloconte.mocktor.valueMatchers.BodyMatchable
import kotlin.String

context(builder: RequestMatcher.Builder.RequestBuilder)
infix fun BodyMatchable.equalToJsonResource(
    path: String,
): JsonContentMatcher {
    val bytes = (this.javaClass.getResource(path)?.readBytes()
                    ?: error("Unable to load resource file '$path'"))
    val contentMatcher = JsonContentMatcher(bytes)
    builder.withBodyMatcher(contentMatcher)
    return contentMatcher
}

context(builder: RequestMatcher.Builder.RequestBuilder)
infix fun BodyMatchable.equalToJson(
    content: String
): JsonContentMatcher {
    val contentMatcher = JsonContentMatcher(content.toByteArray())
    builder.withBodyMatcher(contentMatcher)
    return contentMatcher
}