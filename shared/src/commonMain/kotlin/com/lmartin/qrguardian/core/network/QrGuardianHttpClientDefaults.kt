package com.lmartin.qrguardian.core.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout

object QrGuardianHttpClientDefaults {
    const val REQUEST_TIMEOUT_MILLIS: Long = 15_000
    const val CONNECT_TIMEOUT_MILLIS: Long = 10_000
    const val SOCKET_TIMEOUT_MILLIS: Long = 15_000

    fun apply(config: HttpClientConfig<*>) {
        config.install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
            connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
            socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
        }
    }
}
