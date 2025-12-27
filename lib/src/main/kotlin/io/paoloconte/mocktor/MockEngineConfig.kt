package io.paoloconte.mocktor

import io.ktor.client.engine.HttpClientEngineConfig

/**
 * Configuration object for [MockEngine].
 *
 * Currently a placeholder as MockEngine is a singleton with no configurable options.
 */
object MockEngineConfig: HttpClientEngineConfig()
