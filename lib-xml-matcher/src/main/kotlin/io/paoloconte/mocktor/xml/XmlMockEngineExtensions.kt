package io.paoloconte.mocktor.xml

import io.paoloconte.mocktor.RequestMatcher
import io.paoloconte.mocktor.valueMatchers.BodyMatchable

context(builder: RequestMatcher.Builder.RequestBuilder)
infix fun BodyMatchable.equalToXmlResource(path: String): XmlContentMatcher {
     val bytes = (this.javaClass.getResource(path)?.readBytes()
                    ?: error("Unable to load resource file '$path'"))
    val contentMatcher = XmlContentMatcher(bytes)
    builder.withBodyMatcher(contentMatcher)
    return contentMatcher
}

context(builder: RequestMatcher.Builder.RequestBuilder)
infix fun BodyMatchable.equalToXml(content: String): XmlContentMatcher {
    val contentMatcher = XmlContentMatcher(content.toByteArray())
    builder.withBodyMatcher(contentMatcher)
    return contentMatcher
}
