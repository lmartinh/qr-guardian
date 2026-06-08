package com.lmartin.qrguardian.data.reputation

import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus

class NoOpUrlReputationRepository : UrlReputationRepository {
    override suspend fun checkUrl(url: String): UrlReputationResult = UrlReputationResult(
        status = UrlReputationStatus.NotConfigured,
        provider = "None",
        categories = emptyList(),
        reasons = listOf("Remote reputation checks are not configured."),
    )
}
