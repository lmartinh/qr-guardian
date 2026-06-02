package com.lmartin.qrguardian.data.reputation

import com.lmartin.qrguardian.domain.model.SecurityLevel

class KtorRemoteUrlReputationService(
    httpClient: io.ktor.client.HttpClient
) : BaseKtorRepository(httpClient), RemoteUrlReputationService {
    override suspend fun checkUrl(url: String): RemoteUrlReputationResult {
        return RemoteUrlReputationResult(
            securityLevel = SecurityLevel.Unknown,
            reasons = listOf("Remote URL reputation checks are not wired yet.")
        )
    }
}
