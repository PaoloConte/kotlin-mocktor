package io.paoloconte.mocktor

import io.ktor.client.request.HttpRequestData
import io.ktor.http.content.OutgoingContent


fun HttpRequestData.bodyAsString(): String {
    return (body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString() ?: error("Unsupported body type")
}
