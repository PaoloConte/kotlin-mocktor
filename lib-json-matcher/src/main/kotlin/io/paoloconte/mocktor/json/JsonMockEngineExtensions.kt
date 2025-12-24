package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.RequestMatcher
import kotlin.String

fun RequestMatcher.Builder.RequestBuilder.jsonBodyFromResource(path: String, ignoreFields: Set<String> = emptySet()) {
    bodyFromResource(path)
    withContentMatcher(JsonContentMatcher(ignoreFields))
}

fun RequestMatcher.Builder.RequestBuilder.jsonBody(content: String, ignoreFields: Set<String> = emptySet()) {
    body(content)
    withContentMatcher(JsonContentMatcher(ignoreFields))
}