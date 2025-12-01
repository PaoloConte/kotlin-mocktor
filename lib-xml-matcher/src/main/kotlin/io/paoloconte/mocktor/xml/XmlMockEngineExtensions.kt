package io.paoloconte.mocktor.xml

import io.paoloconte.mocktor.RequestMatcher

fun RequestMatcher.Builder.RequestBuilder.xmlBodyFromResource(path: String) {
    bodyFromResource(path)
    withContentMatcher(XmlContentMatcher)
}

fun RequestMatcher.Builder.RequestBuilder.xmlBody(content: String) {
    body(content)
    withContentMatcher(XmlContentMatcher)
}
