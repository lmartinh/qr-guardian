package com.lmartin.qrguardian.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.head
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class QrGuardianHttpClientFactoryTest {
    @Test
    fun `factory creates working client`() = runBlocking {
        val client = QrGuardianHttpClientFactory.create(
            engineFactory = MockEngine,
            engineConfig = {
                addHandler {
                    respond(
                        content = "",
                        status = HttpStatusCode.OK
                    )
                }
            }
        )

        val response = client.head("https://example.com/health")

        assertEquals(HttpStatusCode.OK, response.status)
    }
}
