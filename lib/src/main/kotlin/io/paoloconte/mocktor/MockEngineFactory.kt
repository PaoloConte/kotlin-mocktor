package io.paoloconte.mocktor

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory

/**
 * Factory for creating [MockEngine] instances.
 *
 * Use this factory when creating an HTTP client:
 * ```kotlin
 * val client = HttpClient(MockEngineFactory) {
 *     // configure client plugins...
 * }
 * ```
 *
 * @see MockEngine
 */
object MockEngineFactory: HttpClientEngineFactory<MockEngineConfig> {
    /**
     * Creates and returns the [MockEngine] instance.
     *
     * @param block Configuration block (currently unused as MockEngine is a singleton).
     * @return The [MockEngine] instance.
     */
    override fun create(block: MockEngineConfig.() -> Unit): HttpClientEngine {
        return MockEngine
    }
}
