package com.lmartin.qrguardian.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory

object QrGuardianHttpClientFactory {
    fun <T : HttpClientEngineConfig> create(
        engineFactory: HttpClientEngineFactory<T>,
        engineConfig: T.() -> Unit = {},
        configure: HttpClientConfig<T>.() -> Unit = {},
    ): HttpClient = HttpClient(engineFactory) {
        engine(engineConfig)
        QrGuardianHttpClientDefaults.apply(this)
        configure()
    }
}
