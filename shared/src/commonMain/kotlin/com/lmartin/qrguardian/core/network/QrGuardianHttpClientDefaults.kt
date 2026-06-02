package com.lmartin.qrguardian.core.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout

object QrGuardianHttpClientDefaults {
    const val RequestTimeoutMillis: Long = 15_000
    const val ConnectTimeoutMillis: Long = 10_000
    const val SocketTimeoutMillis: Long = 15_000

    fun apply(config: HttpClientConfig<*>) {
        config.install(HttpTimeout) {
            requestTimeoutMillis = RequestTimeoutMillis
            connectTimeoutMillis = ConnectTimeoutMillis
            socketTimeoutMillis = SocketTimeoutMillis
        }
    }
}
