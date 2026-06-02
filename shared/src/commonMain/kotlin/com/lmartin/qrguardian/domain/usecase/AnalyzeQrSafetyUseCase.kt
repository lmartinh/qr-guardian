package com.lmartin.qrguardian.domain.usecase

import com.lmartin.qrguardian.domain.analyzer.LocalScanAnalyzer
import com.lmartin.qrguardian.domain.analyzer.DefaultLocalScanAnalyzer
import com.lmartin.qrguardian.domain.classifier.DefaultQrContentClassifier
import com.lmartin.qrguardian.domain.classifier.QrContentClassifier
import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataRepository
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import com.lmartin.qrguardian.domain.metadata.displayName as downloadFileTypeDisplayName
import com.lmartin.qrguardian.domain.metadata.formatFileSize
import com.lmartin.qrguardian.domain.metadata.normalizeUrlForRequest
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanMetadataItem
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.normalizer.DefaultQrTextNormalizer
import com.lmartin.qrguardian.domain.normalizer.QrTextNormalizer
import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationRepository
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AnalyzeQrSafetyUseCase(
    private val textNormalizer: QrTextNormalizer = DefaultQrTextNormalizer(),
    private val contentClassifier: QrContentClassifier = DefaultQrContentClassifier(),
    private val localScanAnalyzer: LocalScanAnalyzer = DefaultLocalScanAnalyzer(),
    private val urlMetadataRepository: UrlMetadataRepository,
    private val urlReputationRepository: UrlReputationRepository
) {
    suspend operator fun invoke(rawText: String): QrAnalysisResult {
        val normalizedText = textNormalizer.normalize(rawText)
        val dangerousScheme = detectDangerousScheme(normalizedText)
        val contentType = if (dangerousScheme != null || normalizedText.isBlank()) {
            QrContentType.Unknown
        } else {
            contentClassifier.classify(normalizedText)
        }

        if (dangerousScheme != null) {
            val localScan = buildDangerousSchemeSection(dangerousScheme)
            return QrAnalysisResult(
                originalText = rawText,
                normalizedText = normalizedText,
                contentType = contentType,
                overallLevel = SecurityLevel.Dangerous,
                canOpen = false,
                localScan = localScan,
                remoteReputation = buildNotApplicableRemoteSection()
            )
        }

        val baseLocalScan = localScanAnalyzer.analyze(
            rawText = rawText,
            normalizedText = normalizedText,
            contentType = contentType
        )

        if (contentType != QrContentType.Url) {
            return QrAnalysisResult(
                originalText = rawText,
                normalizedText = normalizedText,
                contentType = contentType,
                overallLevel = baseLocalScan.level,
                canOpen = false,
                localScan = baseLocalScan,
                remoteReputation = buildNotApplicableRemoteSection()
            )
        }

        val networkUrl = normalizeUrlForRequest(normalizedText)
        return coroutineScope {
            val metadataDeferred = async { loadMetadataSafely(networkUrl) }
            val reputationDeferred = async { checkReputationSafely(networkUrl) }

            val metadataResult = metadataDeferred.await()
            val reputationResult = reputationDeferred.await()

            val localSection = buildLocalSection(baseLocalScan, metadataResult)
            val remoteSection = buildRemoteSection(reputationResult)
            val overallLevel = combineLevels(localSection.level, remoteSection.level)

            QrAnalysisResult(
                originalText = rawText,
                normalizedText = normalizedText,
                contentType = contentType,
                overallLevel = overallLevel,
                canOpen = overallLevel != SecurityLevel.Dangerous,
                localScan = localSection,
                remoteReputation = remoteSection
            )
        }
    }

    private fun buildDangerousSchemeSection(scheme: String): ScanSectionResult {
        return ScanSectionResult(
            name = "Local Scan",
            level = SecurityLevel.Dangerous,
            status = ScanStatus.Completed,
            title = SecurityLevel.Dangerous.title(),
            description = "The scanned content uses a blocked URL scheme.",
            reasons = listOf("The scanned content uses the blocked scheme \"$scheme\"."),
            metadata = emptyList()
        )
    }

    private suspend fun loadMetadataSafely(url: String): UrlMetadataResult {
        return try {
            urlMetadataRepository.fetchMetadata(url)
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Throwable) {
            UrlMetadataResult(
                status = UrlMetadataStatus.Unavailable,
                finalUrl = null,
                contentType = null,
                contentDisposition = null,
                contentLength = null,
                fileName = null,
                fileExtension = null,
                fileType = DownloadFileType.Unknown,
                isLikelyDownload = false,
                reasons = emptyList()
            )
        }
    }

    private suspend fun checkReputationSafely(url: String): UrlReputationResult {
        return try {
            urlReputationRepository.checkUrl(url)
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
    }

    private fun buildLocalSection(
        baseLocalScan: ScanSectionResult,
        metadataResult: UrlMetadataResult
    ): ScanSectionResult {
        val metadataItems = metadataResult.toMetadataItems()
        val metadataReasons = metadataResult.toMetadataReasons()
        val metadataRiskLevel = metadataRiskLevel(metadataResult)
        val sectionLevel = combineLevels(baseLocalScan.level, metadataRiskLevel)
        val reasons = distinctReasons(baseLocalScan.reasons + metadataReasons)

        return baseLocalScan.copy(
            level = sectionLevel,
            title = sectionLevel.title(),
            description = localSectionDescription(sectionLevel, metadataResult),
            reasons = reasons,
            metadata = metadataItems
        )
    }

    private fun buildRemoteSection(remoteResult: UrlReputationResult): ScanSectionResult {
        val status = when (remoteResult.status) {
            UrlReputationStatus.Clean,
            UrlReputationStatus.Suspicious,
            UrlReputationStatus.Malicious,
            UrlReputationStatus.Unknown -> ScanStatus.Completed
            UrlReputationStatus.NotConfigured -> ScanStatus.NotConfigured
            UrlReputationStatus.Error -> ScanStatus.Unavailable
        }
        val level = when (remoteResult.status) {
            UrlReputationStatus.Clean -> SecurityLevel.Safe
            UrlReputationStatus.Suspicious -> SecurityLevel.Suspicious
            UrlReputationStatus.Malicious -> SecurityLevel.Dangerous
            UrlReputationStatus.Unknown,
            UrlReputationStatus.NotConfigured,
            UrlReputationStatus.Error -> SecurityLevel.Unknown
        }
        val reasons = when (remoteResult.status) {
            UrlReputationStatus.NotConfigured -> listOf("Remote reputation checks are not configured.")
            UrlReputationStatus.Error -> listOf("Remote reputation check is currently unavailable.")
            else -> remoteResult.reasons
        }

        return ScanSectionResult(
            name = "Remote Reputation",
            level = level,
            status = status,
            title = remoteSectionTitle(remoteResult.status, level),
            description = remoteSectionDescription(remoteResult.status),
            reasons = distinctReasons(reasons),
            metadata = remoteMetadataItems(remoteResult)
        )
    }

    private fun buildNotApplicableRemoteSection(): ScanSectionResult {
        return ScanSectionResult(
            name = "Remote Reputation",
            level = SecurityLevel.Unknown,
            status = ScanStatus.NotApplicable,
            title = "Remote reputation not applicable",
            description = "Only URLs are checked against remote reputation providers.",
            reasons = emptyList(),
            metadata = emptyList()
        )
    }

    private fun localSectionDescription(
        level: SecurityLevel,
        metadataResult: UrlMetadataResult
    ): String {
        return when {
            metadataResult.status == UrlMetadataStatus.Available && metadataResult.isLikelyDownload ->
                "The destination looks like a downloadable file."
            else -> level.description()
        }
    }

    private fun remoteSectionTitle(
        status: UrlReputationStatus,
        level: SecurityLevel
    ): String {
        return when (status) {
            UrlReputationStatus.NotConfigured -> "Remote reputation not configured"
            UrlReputationStatus.Error -> "Remote reputation unavailable"
            else -> level.title()
        }
    }

    private fun remoteSectionDescription(status: UrlReputationStatus): String {
        return when (status) {
            UrlReputationStatus.NotConfigured ->
                "No remote reputation providers are configured for this QR Guardian instance."
            UrlReputationStatus.Error ->
                "The remote reputation provider could not be reached."
            UrlReputationStatus.Clean ->
                "The remote reputation provider did not report threats for this destination."
            UrlReputationStatus.Suspicious ->
                "The remote reputation provider reported a potential issue with this destination."
            UrlReputationStatus.Malicious ->
                "The remote reputation provider reported a high-risk threat for this destination."
            UrlReputationStatus.Unknown ->
                "The remote reputation provider could not classify this destination."
        }
    }

    private fun remoteMetadataItems(remoteResult: UrlReputationResult): List<ScanMetadataItem> {
        if (remoteResult.status == UrlReputationStatus.NotConfigured || remoteResult.status == UrlReputationStatus.Error) {
            return emptyList()
        }

        val metadata = mutableListOf<ScanMetadataItem>()
        metadata += ScanMetadataItem(label = "Provider", value = remoteResult.provider.ifBlank { "Unknown" })
        if (remoteResult.categories.isNotEmpty()) {
            metadata += ScanMetadataItem(
                label = "Categories",
                value = remoteResult.categories.joinToString(", ") { category ->
                    when (category) {
                        ThreatCategory.Malware -> "Malware"
                        ThreatCategory.Phishing -> "Phishing"
                        ThreatCategory.SocialEngineering -> "Social engineering"
                        ThreatCategory.UnwantedSoftware -> "Unwanted software"
                        ThreatCategory.Unknown -> "Unknown"
                    }
                }
            )
        }
        return metadata
    }

    private fun UrlMetadataResult.toMetadataItems(): List<ScanMetadataItem> {
        if (status != UrlMetadataStatus.Available) {
            return emptyList()
        }

        val metadata = mutableListOf<ScanMetadataItem>()
        finalUrl?.let {
            metadata += ScanMetadataItem(label = "Final URL", value = it)
        }
        contentType?.let {
            metadata += ScanMetadataItem(label = "Content type", value = it)
        }
        contentDisposition?.let {
            metadata += ScanMetadataItem(label = "Content disposition", value = it)
        }
        contentLength?.let {
            metadata += ScanMetadataItem(label = "Content length", value = formatFileSize(it))
        }
        fileName?.let {
            metadata += ScanMetadataItem(label = "File name", value = it)
        }
        fileExtension?.let {
            metadata += ScanMetadataItem(label = "File extension", value = it)
        }
        metadata += ScanMetadataItem(label = "File type", value = fileType.downloadFileTypeDisplayName())
        metadata += ScanMetadataItem(
            label = "Likely download",
            value = if (isLikelyDownload) "Yes" else "No"
        )
        return metadata
    }

    private fun UrlMetadataResult.toMetadataReasons(): List<String> {
        if (status != UrlMetadataStatus.Available) {
            return emptyList()
        }

        val reasons = mutableListOf<String>()
        finalUrl?.let {
            reasons += "The destination redirects to a different URL."
        }
        if (contentDisposition.orEmpty().contains("attachment", ignoreCase = true)) {
            reasons += "The server marks this destination as a downloadable attachment."
        }

        when (fileType) {
            DownloadFileType.AndroidApp,
            DownloadFileType.AppleDiskImage,
            DownloadFileType.WindowsExecutable,
            DownloadFileType.Script -> {
                reasons += "The destination points to an executable or script file."
            }
            DownloadFileType.Archive -> {
                reasons += "The destination points to a downloadable archive."
            }
            DownloadFileType.Unknown,
            DownloadFileType.Pdf,
            DownloadFileType.Document,
            DownloadFileType.Spreadsheet,
            DownloadFileType.Presentation,
            DownloadFileType.Image,
            DownloadFileType.Audio,
            DownloadFileType.Video -> Unit
        }

        return reasons
    }

    private fun metadataRiskLevel(metadataResult: UrlMetadataResult): SecurityLevel? {
        if (metadataResult.status != UrlMetadataStatus.Available) {
            return null
        }

        return when {
            metadataResult.fileType in setOf(
                DownloadFileType.AndroidApp,
                DownloadFileType.AppleDiskImage,
                DownloadFileType.WindowsExecutable,
                DownloadFileType.Script
            ) -> SecurityLevel.Dangerous
            metadataResult.fileType == DownloadFileType.Archive ||
                metadataResult.contentDisposition.orEmpty().contains("attachment", ignoreCase = true) ||
                metadataResult.contentType.equals("application/octet-stream", ignoreCase = true) ->
                SecurityLevel.Suspicious
            else -> null
        }
    }

    private fun combineLevels(localLevel: SecurityLevel, remoteLevel: SecurityLevel?): SecurityLevel {
        return when {
            localLevel == SecurityLevel.Dangerous || remoteLevel == SecurityLevel.Dangerous -> SecurityLevel.Dangerous
            localLevel == SecurityLevel.Suspicious || remoteLevel == SecurityLevel.Suspicious -> SecurityLevel.Suspicious
            localLevel == SecurityLevel.Safe -> SecurityLevel.Safe
            else -> SecurityLevel.Unknown
        }
    }

    private fun detectDangerousScheme(text: String): String? {
        val lowerCaseText = text.trim().lowercase()
        return listOf("javascript:", "file:", "data:", "intent:")
            .firstOrNull { lowerCaseText.startsWith(it) }
    }

    private fun distinctReasons(reasons: List<String>): List<String> {
        val seenReasons = mutableSetOf<String>()
        return reasons.filter { seenReasons.add(it) }
    }
}
