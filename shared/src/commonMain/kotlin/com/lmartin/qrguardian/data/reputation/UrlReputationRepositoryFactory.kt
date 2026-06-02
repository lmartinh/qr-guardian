package com.lmartin.qrguardian.data.reputation

import com.lmartin.qrguardian.data.reputation.google.GoogleSafeBrowsingUrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import io.ktor.client.HttpClient

object UrlReputationRepositoryFactory {
    fun create(
        config: RemoteReputationConfig,
        httpClient: HttpClient
    ): UrlReputationRepository {
        return if (config.isGoogleSafeBrowsingEnabled) {
            GoogleSafeBrowsingUrlReputationRepository(
                httpClient = httpClient,
                apiKey = config.googleSafeBrowsingApiKey.orEmpty()
            )
        } else {
            NoOpUrlReputationRepository()
        }
    }
}
