package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.RequestMatcher

fun RequestMatcher.Builder.jsonBodyFromResource(path: String) {
    bodyFromResource(path)
    withContentMatcher(JsonContentMatcher)
}

fun RequestMatcher.Builder.jsonBody(content: String) {
    body(content)
    withContentMatcher(JsonContentMatcher)
}