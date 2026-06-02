package com.lmartin.qrguardian.data.reputation

import com.lmartin.qrguardian.domain.model.SecurityLevel

interface RemoteUrlReputationService {
    suspend fun checkUrl(url: String): RemoteUrlReputationResult
}

data class RemoteUrlReputationResult(
    val securityLevel: SecurityLevel,
    val reasons: List<String> = emptyList()
)
