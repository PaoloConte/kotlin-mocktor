package io.paoloconte.mocktor.xml

import io.paoloconte.mocktor.RequestMatcher

fun RequestMatcher.Builder.xmlBodyFromResource(path: String) {
    bodyFromResource(path)
    withContentMatcher(XmlContentMatcher)
}

fun RequestMatcher.Builder.xmlBody(content: String) {
    body(content)
    withContentMatcher(XmlContentMatcher)
}
