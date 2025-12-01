package io.paoloconte.mocktor.json

import io.paoloconte.mocktor.RequestMatcher

fun RequestMatcher.Builder.RequestBuilder.jsonBodyFromResource(path: String) {
    bodyFromResource(path)
    withContentMatcher(JsonContentMatcher)
}

fun RequestMatcher.Builder.RequestBuilder.jsonBody(content: String) {
    body(content)
    withContentMatcher(JsonContentMatcher)
}