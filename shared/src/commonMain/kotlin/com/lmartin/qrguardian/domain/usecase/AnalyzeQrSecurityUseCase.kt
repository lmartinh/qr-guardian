package com.lmartin.qrguardian.domain.usecase

import com.lmartin.qrguardian.domain.analyzer.QrSecurityAnalyzer
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.QrSecurityResult
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import kotlin.coroutines.cancellation.CancellationException

class AnalyzeQrSecurityUseCase(
    private val localAnalyzer: QrSecurityAnalyzer,
    private val urlReputationRepository: UrlReputationRepository
) {
    suspend operator fun invoke(rawText: String): QrSecurityResult {
        val localResult = localAnalyzer.analyze(rawText)
        if (localResult.contentType != QrContentType.Url) {
            return localResult.copy(
                remoteReputationStatus = UrlReputationStatus.NotConfigured
            )
        }

        val remoteResult = try {
            urlReputationRepository.checkUrl(localResult.normalizedText)
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Throwable) {
            UrlReputationResult(
                status = UrlReputationStatus.Error,
                provider = "None",
                categories = emptyList(),
                reasons = listOf("Remote reputation check is currently unavailable.")
            )
        }

        return mergeResults(localResult, remoteResult)
    }

    private fun mergeResults(
        localResult: QrSecurityResult,
        remoteResult: UrlReputationResult
    ): QrSecurityResult {
        val mergedReasons = distinctReasons(
            localResult.reasons +
                remoteReasonMessages(remoteResult, localResult.securityLevel) +
                remoteResult.reasons
        )

        val mergedLevel = when (remoteResult.status) {
            UrlReputationStatus.Malicious -> SecurityLevel.Dangerous
            UrlReputationStatus.Suspicious -> if (localResult.securityLevel == SecurityLevel.Dangerous) {
                SecurityLevel.Dangerous
            } else {
                SecurityLevel.Suspicious
            }
            UrlReputationStatus.Clean -> localResult.securityLevel
            UrlReputationStatus.Unknown -> localResult.securityLevel
            UrlReputationStatus.NotConfigured -> localResult.securityLevel
            UrlReputationStatus.Error -> localResult.securityLevel
        }

        val canOpen = when (mergedLevel) {
            SecurityLevel.Dangerous -> false
            SecurityLevel.Suspicious -> true
            SecurityLevel.Safe -> true
            SecurityLevel.Unknown -> true
        }

        return localResult.copy(
            securityLevel = mergedLevel,
            title = mergedLevel.title(),
            description = mergedLevel.description(),
            reasons = mergedReasons,
            canOpen = canOpen,
            remoteReputationStatus = remoteResult.status
        )
    }

    private fun remoteReasonMessages(
        remoteResult: UrlReputationResult,
        localLevel: SecurityLevel
    ): List<String> {
        return when (remoteResult.status) {
            UrlReputationStatus.NotConfigured -> listOf("Remote reputation checks are not configured.")
            UrlReputationStatus.Error -> listOf("Remote reputation check is currently unavailable.")
            UrlReputationStatus.Clean -> if (localLevel == SecurityLevel.Safe) {
                listOf("No threats were reported by the external reputation service.")
            } else {
                emptyList()
            }
            UrlReputationStatus.Suspicious, UrlReputationStatus.Malicious, UrlReputationStatus.Unknown -> emptyList()
        }
    }

    private fun distinctReasons(reasons: List<String>): List<String> {
        val seenReasons = mutableSetOf<String>()
        return reasons.filter { seenReasons.add(it) }
    }
}
