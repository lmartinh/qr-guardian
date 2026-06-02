package com.lmartin.qrguardian.data.reputation

import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.get

abstract class BaseKtorRepository(
    protected val httpClient: HttpClient
) {
    protected suspend fun getText(url: String): String {
        return httpClient.get(url).bodyAsText()
    }
}
