package com.lmartin.qrguardian.domain.usecase

import com.lmartin.qrguardian.domain.metadata.DownloadFileType
import com.lmartin.qrguardian.domain.metadata.UrlMetadataResult
import com.lmartin.qrguardian.domain.metadata.UrlMetadataStatus
import com.lmartin.qrguardian.domain.metadata.isAttachmentDisposition
import com.lmartin.qrguardian.domain.metadata.shouldShowPath
import com.lmartin.qrguardian.domain.model.ScanMetadataItem
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.domain.metadata.UrlResourceKind
import com.lmartin.qrguardian.domain.reputation.ThreatCategory
import com.lmartin.qrguardian.domain.reputation.UrlReputationResult
import com.lmartin.qrguardian.domain.reputation.UrlReputationStatus
import com.lmartin.qrguardian.domain.rules.url.parseUrl
import com.lmartin.qrguardian.domain.metadata.displayName as downloadFileTypeDisplayName

internal object QrSafetyAnalysisAssembler {
    fun buildDangerousSchemeSection(scheme: String): ScanSectionResult = ScanSectionResult(
        name = "Local Scan",
        level = SecurityLevel.Dangerous,
        status = ScanStatus.Completed,
        title = SecurityLevel.Dangerous.title(),
        description = "The scanned content uses a blocked URL scheme.",
        reasons = listOf("The scanned content uses the blocked scheme \"$scheme\"."),
        metadata = emptyList(),
    )

    fun buildNotApplicableRemoteSection(): ScanSectionResult = ScanSectionResult(
        name = "Remote Reputation",
        level = SecurityLevel.Unknown,
        status = ScanStatus.NotApplicable,
        title = "Remote reputation not applicable",
        description = "Only URLs are checked against remote reputation providers.",
        reasons = emptyList(),
        metadata = emptyList(),
    )

    fun buildLocalSection(
        baseLocalScan: ScanSectionResult,
        metadataResult: UrlMetadataResult,
        sourceUrl: String,
    ): ScanSectionResult {
        val metadataItems = metadataResult.toMetadataItems(sourceUrl)
        val metadataReasons = metadataResult.toMetadataReasons()
        val metadataRiskLevel = metadataRiskLevel(metadataResult)
        val sectionLevel = combineLevels(baseLocalScan.level, metadataRiskLevel)
        val reasons = distinctReasons(baseLocalScan.reasons + metadataReasons)

        return baseLocalScan.copy(
            level = sectionLevel,
            title = sectionLevel.title(),
            description = localSectionDescription(level = sectionLevel, metadataResult = metadataResult),
            reasons = reasons,
            metadata = metadataItems,
        )
    }

    fun buildRemoteSection(remoteResult: UrlReputationResult): ScanSectionResult {
        val status =
            when (remoteResult.status) {
                UrlReputationStatus.Clean,
                UrlReputationStatus.Suspicious,
                UrlReputationStatus.Malicious,
                UrlReputationStatus.Unknown,
                -> ScanStatus.Completed

                UrlReputationStatus.NotConfigured -> ScanStatus.NotConfigured

                UrlReputationStatus.Error -> ScanStatus.Unavailable
            }
        val level =
            when (remoteResult.status) {
                UrlReputationStatus.Clean -> SecurityLevel.Safe

                UrlReputationStatus.Suspicious -> SecurityLevel.Suspicious

                UrlReputationStatus.Malicious -> SecurityLevel.Dangerous

                UrlReputationStatus.Unknown,
                UrlReputationStatus.NotConfigured,
                UrlReputationStatus.Error,
                -> SecurityLevel.Unknown
            }
        val reasons =
            when (remoteResult.status) {
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
            metadata = remoteMetadataItems(remoteResult),
        )
    }

    fun combineLevels(
        localLevel: SecurityLevel,
        remoteLevel: SecurityLevel?,
    ): SecurityLevel = when {
        localLevel == SecurityLevel.Dangerous || remoteLevel == SecurityLevel.Dangerous -> SecurityLevel.Dangerous
        localLevel == SecurityLevel.Suspicious || remoteLevel == SecurityLevel.Suspicious -> SecurityLevel.Suspicious
        localLevel == SecurityLevel.Safe -> SecurityLevel.Safe
        else -> SecurityLevel.Unknown
    }

    private fun localSectionDescription(
        level: SecurityLevel,
        metadataResult: UrlMetadataResult,
    ): String = when {
        metadataResult.status == UrlMetadataStatus.Available &&
            metadataResult.fileType in
            setOf(
                DownloadFileType.AndroidApp,
                DownloadFileType.AppleDiskImage,
                DownloadFileType.WindowsExecutable,
                DownloadFileType.Script,
            ) -> {
            "This file can install software or run code on your device."
        }

        metadataResult.status == UrlMetadataStatus.Available && metadataResult.isLikelyDownload -> {
            "This link looks like a file download."
        }

        else -> {
            level.description()
        }
    }

    private fun remoteSectionTitle(
        status: UrlReputationStatus,
        level: SecurityLevel,
    ): String = when (status) {
        UrlReputationStatus.NotConfigured -> "Remote reputation not configured"
        UrlReputationStatus.Error -> "Remote reputation unavailable"
        else -> level.title()
    }

    private fun remoteSectionDescription(status: UrlReputationStatus): String = when (status) {
        UrlReputationStatus.NotConfigured -> {
            "No remote reputation providers are configured for this QR Guardian instance."
        }

        UrlReputationStatus.Error -> {
            "The remote reputation provider could not be reached."
        }

        UrlReputationStatus.Clean -> {
            "The remote reputation provider did not report threats for this destination."
        }

        UrlReputationStatus.Suspicious -> {
            "The remote reputation provider reported a potential issue with this destination."
        }

        UrlReputationStatus.Malicious -> {
            "The remote reputation provider reported a high-risk threat for this destination."
        }

        UrlReputationStatus.Unknown -> {
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
            metadata +=
                ScanMetadataItem(
                    label = "Categories",
                    value =
                    remoteResult.categories.joinToString(", ") { category ->
                        when (category) {
                            ThreatCategory.Malware -> "Malware"
                            ThreatCategory.Phishing -> "Phishing"
                            ThreatCategory.SocialEngineering -> "Social engineering"
                            ThreatCategory.UnwantedSoftware -> "Unwanted software"
                            ThreatCategory.Unknown -> "Unknown"
                        }
                    },
                )
        }
        return metadata
    }

    private fun UrlMetadataResult.toMetadataItems(sourceUrl: String): List<ScanMetadataItem> {
        val metadata = mutableListOf<ScanMetadataItem>()

        addUrlIdentityMetadata(sourceUrl, metadata)

        if (status != UrlMetadataStatus.Available) {
            return metadata
        }

        displayContentLabel(contentType, fileType, resourceKind)?.let { contentLabel ->
            metadata += ScanMetadataItem(label = "Content", value = contentLabel)
        }
        finalUrl?.let {
            metadata += ScanMetadataItem(label = "Resolved destination", value = it)
        }
        fileName?.let {
            metadata += ScanMetadataItem(label = "File name", value = it)
        }
        fileType.takeIf { it != DownloadFileType.Unknown }?.let {
            metadata += ScanMetadataItem(label = "File type", value = it.downloadFileTypeDisplayName())
        }
        if (fileType == DownloadFileType.Unknown && fileName != null) {
            metadata += ScanMetadataItem(label = "File type", value = "File")
        }
        if (shouldShowDownloadMetadata()) {
            metadata +=
                ScanMetadataItem(
                    label = "Download",
                    value = if (isAttachmentDisposition(contentDisposition)) {
                        "Server suggests a file download"
                    } else {
                        "Downloadable file"
                    },
                )
        }
        return metadata
    }

    private fun UrlMetadataResult.toMetadataReasons(): List<String> {
        if (status != UrlMetadataStatus.Available) {
            return emptyList()
        }

        val reasons = mutableListOf<String>()
        finalUrl?.let {
            reasons += "This destination resolves to a different URL."
        }
        if (contentDisposition.orEmpty().contains("attachment", ignoreCase = true)) {
            reasons += "The server suggests downloading this file."
        }

        when (resourceKind) {
            UrlResourceKind.Archive -> {
                reasons += "Compressed files can hide other files inside."
            }

            UrlResourceKind.InstallerOrExecutable -> {
                reasons += "This file can run code or install software on your device."
            }

            UrlResourceKind.UnknownBinary -> {
                reasons += "The destination appears to be a generic binary download."
            }

            UrlResourceKind.OtherFile,
            UrlResourceKind.Document,
            UrlResourceKind.Image,
            UrlResourceKind.Media,
            UrlResourceKind.WebPage,
            UrlResourceKind.Unknown,
            -> {
                Unit
            }
        }

        when (fileType) {
            DownloadFileType.AndroidApp,
            DownloadFileType.AppleDiskImage,
            DownloadFileType.WindowsExecutable,
            DownloadFileType.Script,
            -> {
                if (resourceKind != UrlResourceKind.InstallerOrExecutable) {
                    reasons += "This file can run code or install software on your device."
                }
            }

            DownloadFileType.Unknown,
            DownloadFileType.Pdf,
            DownloadFileType.Document,
            DownloadFileType.Spreadsheet,
            DownloadFileType.Presentation,
            DownloadFileType.Archive,
            DownloadFileType.Image,
            DownloadFileType.Audio,
            DownloadFileType.Video,
            -> {
                Unit
            }
        }

        return reasons
    }

    private fun UrlMetadataResult.addUrlIdentityMetadata(
        sourceUrl: String,
        metadata: MutableList<ScanMetadataItem>,
    ) {
        val parsedUrl = parseUrl(sourceUrl)
        if (parsedUrl.host.isNotBlank()) {
            metadata += ScanMetadataItem(label = "Host", value = parsedUrl.host)
        }
        parsedUrl.scheme?.let { scheme ->
            val connectionLabel =
                when (scheme.lowercase()) {
                    "https" -> "HTTPS"
                    "http" -> "HTTP"
                    else -> scheme.uppercase()
                }
            metadata += ScanMetadataItem(label = "Connection", value = connectionLabel)
        }

        val path = parsedUrl.path.takeIf { it.isNotBlank() && it != "/" }
        if (path != null && shouldShowPath(path, fileName, resourceKind)) {
            metadata += ScanMetadataItem(label = "Path", value = path)
        }
    }

    private fun UrlMetadataResult.shouldShowFileDetails(): Boolean {
        if (status != UrlMetadataStatus.Available) {
            return false
        }

        return fileName != null || fileType != DownloadFileType.Unknown
    }

    private fun UrlMetadataResult.shouldShowDownloadMetadata(): Boolean {
        if (status != UrlMetadataStatus.Available) {
            return false
        }

        return isLikelyDownload || isAttachmentDisposition(contentDisposition)
    }

    private fun displayContentLabel(
        contentType: String?,
        fileType: DownloadFileType,
        resourceKind: UrlResourceKind,
    ): String? {
        val normalizedContentType = contentType.orEmpty()

        return when {
            normalizedContentType.equals("text/html", ignoreCase = true) ||
                normalizedContentType.equals("application/xhtml+xml", ignoreCase = true) -> {
                "Web page"
            }

            normalizedContentType.equals("application/pdf", ignoreCase = true) || fileType == DownloadFileType.Pdf -> {
                "PDF document"
            }

            normalizedContentType.startsWith("application/zip", ignoreCase = true) ||
                normalizedContentType.contains("7z", ignoreCase = true) ||
                normalizedContentType.contains("tar", ignoreCase = true) ||
                fileType == DownloadFileType.Archive ||
                resourceKind == UrlResourceKind.Archive -> {
                "Archive"
            }

            normalizedContentType.startsWith("image/", ignoreCase = true) || fileType == DownloadFileType.Image || resourceKind == UrlResourceKind.Image -> {
                "Image"
            }

            normalizedContentType.startsWith("audio/", ignoreCase = true) || fileType == DownloadFileType.Audio -> {
                "Audio"
            }

            normalizedContentType.startsWith("video/", ignoreCase = true) || fileType == DownloadFileType.Video -> {
                "Video"
            }

            normalizedContentType.equals("application/vnd.android.package-archive", ignoreCase = true) ||
                fileType == DownloadFileType.AndroidApp -> {
                "Android app"
            }

            fileType == DownloadFileType.Document -> {
                "Document"
            }

            fileType == DownloadFileType.Spreadsheet -> {
                "Spreadsheet"
            }

            fileType == DownloadFileType.Presentation -> {
                "Presentation"
            }

            fileType != DownloadFileType.Unknown -> {
                fileType.downloadFileTypeDisplayName()
            }

            normalizedContentType.equals("application/octet-stream", ignoreCase = true) -> {
                "Unknown binary file"
            }

            fileType == DownloadFileType.Unknown && contentType.isNullOrBlank() -> null

            else -> {
                "File"
            }
        }
    }

    private fun metadataRiskLevel(metadataResult: UrlMetadataResult): SecurityLevel? {
        if (metadataResult.status != UrlMetadataStatus.Available) {
            return null
        }

        return when {
            metadataResult.resourceKind == UrlResourceKind.InstallerOrExecutable -> {
                SecurityLevel.Dangerous
            }

            metadataResult.resourceKind == UrlResourceKind.Archive ||
                metadataResult.resourceKind == UrlResourceKind.UnknownBinary ||
                isAttachmentDisposition(metadataResult.contentDisposition) ||
                metadataResult.contentType.equals("application/octet-stream", ignoreCase = true) -> {
                SecurityLevel.Suspicious
            }

            else -> {
                null
            }
        }
    }

    private fun distinctReasons(reasons: List<String>): List<String> {
        val seenReasons = mutableSetOf<String>()
        return reasons.filter { seenReasons.add(it) }
    }
}
