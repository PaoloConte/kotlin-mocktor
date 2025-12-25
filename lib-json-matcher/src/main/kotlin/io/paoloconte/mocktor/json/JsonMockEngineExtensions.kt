package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.RequestMatcher
import kotlin.String

fun RequestMatcher.Builder.RequestBuilder.jsonBodyFromResource(
    path: String,
    ignoreFields: Set<String> = emptySet(),
    ignoreUnknownKeys: Boolean = false
) {
    bodyFromResource(path)
    withContentMatcher(JsonContentMatcher(ignoreFields, ignoreUnknownKeys))
}

fun RequestMatcher.Builder.RequestBuilder.jsonBody(
    content: String,
    ignoreFields: Set<String> = emptySet(),
    ignoreUnknownKeys: Boolean = false
) {
    body(content)
    withContentMatcher(JsonContentMatcher(ignoreFields, ignoreUnknownKeys))
}