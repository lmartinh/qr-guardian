package com.lmartin.qrguardian.domain.reputation

interface UrlReputationRepository {
    suspend fun checkUrl(url: String): UrlReputationResult
}
