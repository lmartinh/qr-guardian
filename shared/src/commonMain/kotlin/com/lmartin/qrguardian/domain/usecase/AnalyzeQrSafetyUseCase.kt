package com.lmartin.qrguardian.domain.usecase

import com.lmartin.qrguardian.domain.analyzer.DefaultLocalScanAnalyzer
import com.lmartin.qrguardian.domain.analyzer.LocalScanAnalyzer
import com.lmartin.qrguardian.domain.classifier.DefaultQrContentClassifier
import com.lmartin.qrguardian.domain.classifier.QrContentClassifier
import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataRepository
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import com.lmartin.qrguardian.domain.metadata.normalizeUrlForRequest
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.normalizer.DefaultQrTextNormalizer
import com.lmartin.qrguardian.domain.normalizer.QrTextNormalizer
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
    private val urlReputationRepository: UrlReputationRepository,
) {
    suspend operator fun invoke(rawText: String): QrAnalysisResult {
        val normalizedText = textNormalizer.normalize(rawText)
        val dangerousScheme = DangerousSchemeDetector.detect(normalizedText)
        val contentType =
            if (dangerousScheme != null || normalizedText.isBlank()) {
                QrContentType.Unknown
            } else {
                contentClassifier.classify(normalizedText)
            }

        if (dangerousScheme != null) {
            return QrAnalysisResult(
                originalText = rawText,
                normalizedText = normalizedText,
                openableUrl = null,
                contentType = contentType,
                overallLevel = SecurityLevel.Dangerous,
                canOpen = false,
                localScan = QrSafetyAnalysisAssembler.buildDangerousSchemeSection(dangerousScheme),
                remoteReputation = QrSafetyAnalysisAssembler.buildNotApplicableRemoteSection(),
            )
        }

        val baseLocalScan =
            localScanAnalyzer.analyze(
                rawText = rawText,
                normalizedText = normalizedText,
                contentType = contentType,
            )

        if (contentType != QrContentType.Url) {
            return QrAnalysisResult(
                originalText = rawText,
                normalizedText = normalizedText,
                openableUrl = null,
                contentType = contentType,
                overallLevel = baseLocalScan.level,
                canOpen = false,
                localScan = baseLocalScan,
                remoteReputation = QrSafetyAnalysisAssembler.buildNotApplicableRemoteSection(),
            )
        }

        val networkUrl = normalizeUrlForRequest(normalizedText)
        return coroutineScope {
            val metadataDeferred = async { loadMetadataSafely(networkUrl) }
            val reputationDeferred = async { checkReputationSafely(networkUrl) }

            val metadataResult = metadataDeferred.await()
            val reputationResult = reputationDeferred.await()
            val localSection = QrSafetyAnalysisAssembler.buildLocalSection(baseLocalScan, metadataResult, networkUrl)
            val remoteSection = QrSafetyAnalysisAssembler.buildRemoteSection(reputationResult)
            val overallLevel = QrSafetyAnalysisAssembler.combineLevels(localSection.level, remoteSection.level)

            QrAnalysisResult(
                originalText = rawText,
                normalizedText = normalizedText,
                openableUrl = if (overallLevel != SecurityLevel.Dangerous) networkUrl else null,
                contentType = contentType,
                overallLevel = overallLevel,
                canOpen = overallLevel != SecurityLevel.Dangerous,
                localScan = localSection,
                remoteReputation = remoteSection,
            )
        }
    }

    private suspend fun loadMetadataSafely(url: String): UrlMetadataResult = try {
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
            reasons = emptyList(),
        )
    }

    private suspend fun checkReputationSafely(url: String): UrlReputationResult = try {
        urlReputationRepository.checkUrl(url)
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (exception: Throwable) {
        UrlReputationResult(
            status = UrlReputationStatus.Error,
            provider = "None",
            categories = emptyList(),
            reasons = listOf("Remote reputation check is currently unavailable."),
        )
    }
}
