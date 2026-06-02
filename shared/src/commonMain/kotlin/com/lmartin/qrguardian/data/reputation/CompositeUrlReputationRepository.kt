package com.lmartin.qrguardian.data.reputation

import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import kotlin.coroutines.cancellation.CancellationException

class CompositeUrlReputationRepository(
    private val repositories: List<UrlReputationRepository>
) : UrlReputationRepository {
    override suspend fun checkUrl(url: String): UrlReputationResult {
        if (repositories.isEmpty()) {
            return UrlReputationResult(
                status = UrlReputationStatus.NotConfigured,
                provider = "None",
                categories = emptyList(),
                reasons = listOf("Remote reputation checks are not configured.")
            )
        }

        val results = repositories.map { repository ->
            runCatching {
                repository.checkUrl(url)
            }.getOrElse { exception ->
                if (exception is CancellationException) {
                    throw exception
                }
                UrlReputationResult(
                    status = UrlReputationStatus.Error,
                    provider = "Unknown",
                    categories = emptyList(),
                    reasons = listOf("A remote reputation check is currently unavailable.")
                )
            }
        }

        return merge(results)
    }

    private fun merge(results: List<UrlReputationResult>): UrlReputationResult {
        if (results.isEmpty()) {
            return UrlReputationResult(
                status = UrlReputationStatus.NotConfigured,
                provider = "None",
                categories = emptyList(),
                reasons = listOf("Remote reputation checks are not configured.")
            )
        }

        val distinctProviders = results
            .map { it.provider }
            .filter { it.isNotBlank() && it != "None" && it != "Unknown" }
            .distinct()

        val combinedProvider = when (distinctProviders.size) {
            0 -> "Multiple providers"
            1 -> distinctProviders.first()
            else -> distinctProviders.joinToString(", ")
        }

        val categories = results
            .flatMap { it.categories }
            .distinct()

        val reasons = distinctReasons(
            results.flatMap { result -> result.reasons }
        )

        val nonInformationalResults = results.filterNot {
            it.status == UrlReputationStatus.NotConfigured || it.status == UrlReputationStatus.Error
        }
        val errors = results.filter { it.status == UrlReputationStatus.Error }

        val status = when {
            results.any { it.status == UrlReputationStatus.Malicious } -> UrlReputationStatus.Malicious
            results.any { it.status == UrlReputationStatus.Suspicious } -> UrlReputationStatus.Suspicious
            results.any { it.status == UrlReputationStatus.Unknown } -> UrlReputationStatus.Unknown
            nonInformationalResults.any { it.status == UrlReputationStatus.Clean } -> UrlReputationStatus.Clean
            errors.isNotEmpty() && nonInformationalResults.isEmpty() && results.any { it.status != UrlReputationStatus.NotConfigured } -> UrlReputationStatus.Error
            results.all { it.status == UrlReputationStatus.NotConfigured } -> UrlReputationStatus.NotConfigured
            results.any { it.status == UrlReputationStatus.Error } && results.none { it.status != UrlReputationStatus.Error && it.status != UrlReputationStatus.NotConfigured } -> UrlReputationStatus.Error
            else -> UrlReputationStatus.Unknown
        }

        return UrlReputationResult(
            status = status,
            provider = combinedProvider,
            categories = categories,
            reasons = reasons
        )
    }

    private fun distinctReasons(reasons: List<String>): List<String> {
        val seenReasons = mutableSetOf<String>()
        return reasons.filter { seenReasons.add(it) }
    }
}
