package io.paoloconte.mocktor

import io.ktor.client.request.HttpRequestData
import io.ktor.http.content.OutgoingContent

/**
 * Returns the request body as a UTF-8 string.
 *
 * @return The body content as a string.
 * @throws IllegalStateException If the body is not available as a ByteArray.
 */
fun HttpRequestData.bodyAsString(): String {
    return (body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString() ?: error("Unsupported body type")
}

/**
 * Returns the request body as a byte array, or null if not available.
 *
 * @return The body content as bytes, or null if the body type is not supported.
 */
fun HttpRequestData.bodyAsBytesOrNull(): ByteArray? {
    return (body as? OutgoingContent.ByteArrayContent)?.bytes()
}
