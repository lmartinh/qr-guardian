package com.lmartin.qrguardian.domain.usecase

import com.lmartin.qrguardian.data.reputation.NoOpUrlReputationRepository
import com.lmartin.qrguardian.domain.fixtures.qrSampleCases
import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataRepository
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.presentation.result.ResultUiState
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QrSampleDatasetRegressionTest {
    private val useCase =
        AnalyzeQrSafetyUseCase(
            urlMetadataRepository = SampleUrlMetadataRepository(),
            urlReputationRepository = NoOpUrlReputationRepository(),
        )

    @Test
    fun `sample qr payloads keep their expected security behavior`() = runBlocking {
        qrSampleCases.forEach { sampleCase ->
            val result = useCase(sampleCase.rawText)
            val uiState = ResultUiState.success(result)

            assertEquals(sampleCase.expectedContentType, result.contentType, sampleCase.name)
            assertEquals(sampleCase.expectedOverallLevel, result.overallLevel, sampleCase.name)
            assertEquals(sampleCase.expectedLocalLevel, result.localScan.level, sampleCase.name)
            assertEquals(sampleCase.expectedCanOpen, result.canOpen, sampleCase.name)
            assertEquals(sampleCase.expectedShowOpenButton, uiState.showOpenButton, sampleCase.name)
            assertEquals(sampleCase.expectedOpenableUrl, result.openableUrl, sampleCase.name)
            assertEquals(expectedRemoteStatus(result.contentType), result.remoteReputation.status, sampleCase.name)

            assertTrue(
                sampleCase.expectedLocalReasonsContain.all { expectedReason ->
                    result.localScan.reasons.any { actualReason -> actualReason.contains(expectedReason) }
                },
                sampleCase.name + " local reasons were " + result.localScan.reasons.joinToString(),
            )
            assertTrue(
                sampleCase.expectedLocalMetadataContain.all { (expectedLabel, expectedValue) ->
                    result.localScan.metadata.any { metadata ->
                        metadata.label == expectedLabel && metadata.value == expectedValue
                    }
                },
                sampleCase.name + " local metadata were " + result.localScan.metadata.joinToString(),
            )
        }
    }

    private fun expectedRemoteStatus(contentType: QrContentType): ScanStatus = if (contentType == QrContentType.Url) {
        ScanStatus.NotConfigured
    } else {
        ScanStatus.NotApplicable
    }

    private class SampleUrlMetadataRepository : UrlMetadataRepository {
        override suspend fun fetchMetadata(url: String): UrlMetadataResult = when {
            url.endsWith("/menu.pdf") -> {
                metadata(
                    contentType = "application/pdf",
                    fileName = "menu.pdf",
                    fileExtension = "pdf",
                    fileType = DownloadFileType.Pdf,
                    isLikelyDownload = false,
                )
            }

            url.endsWith("/archive.zip") -> {
                metadata(
                    contentType = "application/zip",
                    fileName = "archive.zip",
                    fileExtension = "zip",
                    fileType = DownloadFileType.Archive,
                    isLikelyDownload = true,
                )
            }

            url.endsWith("/download/app.apk") -> {
                metadata(
                    contentType = "application/vnd.android.package-archive",
                    fileName = "app.apk",
                    fileExtension = "apk",
                    fileType = DownloadFileType.AndroidApp,
                    isLikelyDownload = true,
                )
            }

            url.endsWith("/setup.exe") -> {
                metadata(
                    contentType = "application/octet-stream",
                    fileName = "setup.exe",
                    fileExtension = "exe",
                    fileType = DownloadFileType.WindowsExecutable,
                    isLikelyDownload = true,
                )
            }

            else -> {
                unavailableMetadata()
            }
        }

        private fun metadata(
            contentType: String?,
            fileName: String?,
            fileExtension: String?,
            fileType: DownloadFileType,
            isLikelyDownload: Boolean,
            resourceKind: com.lmartin.qrguardian.domain.metadata.UrlResourceKind = when (fileType) {
                DownloadFileType.Pdf,
                DownloadFileType.Document,
                DownloadFileType.Spreadsheet,
                DownloadFileType.Presentation,
                -> com.lmartin.qrguardian.domain.metadata.UrlResourceKind.Document

                DownloadFileType.Image -> com.lmartin.qrguardian.domain.metadata.UrlResourceKind.Image
                DownloadFileType.Audio,
                DownloadFileType.Video,
                -> com.lmartin.qrguardian.domain.metadata.UrlResourceKind.Media

                DownloadFileType.Archive -> com.lmartin.qrguardian.domain.metadata.UrlResourceKind.Archive
                DownloadFileType.AndroidApp,
                DownloadFileType.AppleDiskImage,
                DownloadFileType.WindowsExecutable,
                DownloadFileType.Script,
                -> com.lmartin.qrguardian.domain.metadata.UrlResourceKind.InstallerOrExecutable

                DownloadFileType.Unknown -> com.lmartin.qrguardian.domain.metadata.UrlResourceKind.Unknown
            },
        ): UrlMetadataResult = UrlMetadataResult(
            status = UrlMetadataStatus.Available,
            finalUrl = null,
            contentType = contentType,
            contentDisposition = null,
            contentLength = null,
            fileName = fileName,
            fileExtension = fileExtension,
            fileType = fileType,
            isLikelyDownload = isLikelyDownload,
            reasons = emptyList(),
            resourceKind = resourceKind,
        )

        private fun unavailableMetadata(): UrlMetadataResult = UrlMetadataResult(
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
}
