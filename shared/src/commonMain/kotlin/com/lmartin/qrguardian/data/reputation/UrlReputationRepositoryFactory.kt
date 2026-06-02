package com.lmartin.qrguardian.data.reputation

import com.lmartin.qrguardian.data.reputation.google.GoogleSafeBrowsingUrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import io.ktor.client.HttpClient

object UrlReputationRepositoryFactory {
    fun create(
        config: RemoteReputationConfig,
        httpClient: HttpClient
    ): UrlReputationRepository {
        val repositories = buildList<UrlReputationRepository> {
            if (config.isGoogleSafeBrowsingEnabled) {
                add(
                    GoogleSafeBrowsingUrlReputationRepository(
                        httpClient = httpClient,
                        apiKey = config.googleSafeBrowsingApiKey.orEmpty()
                    )
                )
            }
            if (config.isUrlHausEnabled) {
                add(
                    UrlHausReputationRepository(
                        httpClient = httpClient,
                        apiKey = config.urlHausApiKey.orEmpty()
                    )
                )
            }
        }

        return when (repositories.size) {
            0 -> NoOpUrlReputationRepository()
            1 -> repositories.first()
            else -> CompositeUrlReputationRepository(repositories)
        }
    }
}
