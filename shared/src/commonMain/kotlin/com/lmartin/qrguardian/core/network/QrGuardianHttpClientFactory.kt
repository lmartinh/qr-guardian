package com.lmartin.qrguardian.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.HttpClientEngineConfig

object QrGuardianHttpClientFactory {
    fun <T : HttpClientEngineConfig> create(
        engineFactory: HttpClientEngineFactory<T>,
        engineConfig: T.() -> Unit = {},
        configure: HttpClientConfig<T>.() -> Unit = {}
    ): HttpClient {
        return HttpClient(engineFactory) {
            engine(engineConfig)
            QrGuardianHttpClientDefaults.apply(this)
            configure()
        }
    }
}
