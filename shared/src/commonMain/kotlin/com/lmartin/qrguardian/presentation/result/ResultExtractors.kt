package com.lmartin.qrguardian.presentation.result

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.theme.QrGuardianColors

internal fun buildLocalAnalysisDetails(
    analysis: QrAnalysisResult,
    texts: ResultTexts,
): List<ResultDetailItem> {
    val items = mutableListOf<ResultDetailItem>()

    if (analysis.contentType == QrContentType.Url) {
        items += buildUrlLocalDetails(analysis, texts)
    } else {
        items +=
            detailItem(
                icon = contentTypeIcon(analysis.contentType),
                label = texts.detailType,
                value = contentTypeLabel(analysis.contentType, texts),
            )
        items +=
            detailItem(
                icon = actionIcon(analysis.contentType),
                label = texts.detailAction,
                value = contentActionLabel(analysis.contentType, analysis.openableUrl, texts),
            )
        items += buildNonUrlLocalDetails(analysis, texts)
    }

    analysis.localScan.metadata.forEach { metadata ->
        items +=
            detailItem(
                icon = metadataIcon(metadata.label),
                label = metadataDisplayLabel(metadata.label, texts),
                value = metadataDisplayValue(metadata.label, metadata.value, texts),
            )
    }

    return items
}

internal fun buildUrlLocalDetails(
    analysis: QrAnalysisResult,
    texts: ResultTexts,
): List<ResultDetailItem> = listOf(
    detailItem(
        icon = contentTypeIcon(analysis.contentType),
        label = texts.detailType,
        value = contentTypeLabel(analysis.contentType, texts),
    ),
    detailItem(
        icon = actionIcon(analysis.contentType),
        label = texts.detailAction,
        value = urlActionLabel(analysis, texts),
    ),
)

internal fun buildLocalSignals(section: ScanSectionResult): List<String> = section.reasons

internal fun buildRemoteAnalysisDetails(
    section: ScanSectionResult,
    texts: ResultTexts,
): List<ResultDetailItem> {
    if (section.status != ScanStatus.Completed) {
        return emptyList()
    }

    val items = mutableListOf<ResultDetailItem>()

    items +=
        detailItem(
            icon = Icons.Filled.Info,
            label = texts.detailState,
            value = texts.remoteCompleted,
            color = section.level.toSignalColor(),
        )

    section.reasons.take(2).forEach { reason ->
        if (items.size < 3) {
            items += detailItem(Icons.Filled.Warning, texts.detailUsage, reason, section.level.toSignalColor())
        }
    }

    section.metadata.take(1).forEach { metadata ->
        if (items.size < 3) {
            items += detailItem(Icons.Filled.Warning, metadata.label, metadata.value)
        }
    }

    return items.take(3)
}

internal fun SecurityLevel.toSignalColor(): androidx.compose.ui.graphics.Color = when (this) {
    SecurityLevel.Safe -> androidx.compose.ui.graphics.Color(0xFF10B981)
    SecurityLevel.Suspicious -> androidx.compose.ui.graphics.Color(0xFF9A6B00)
    SecurityLevel.Dangerous -> androidx.compose.ui.graphics.Color(0xFFB42318)
    SecurityLevel.Unknown -> androidx.compose.ui.graphics.Color(0xFF6B7280)
}

private fun buildNonUrlLocalDetails(
    analysis: QrAnalysisResult,
    texts: ResultTexts,
): List<ResultDetailItem> = when (analysis.contentType) {
    QrContentType.Email -> listOf(
        detailItem(Icons.Filled.Info, texts.detailDestination, extractMailTarget(analysis.normalizedText, texts)),
        detailItem(Icons.Filled.Info, texts.detailExtraField, extractMailExtras(analysis.normalizedText, texts)),
    )

    QrContentType.Phone -> listOf(
        detailItem(Icons.Filled.Info, texts.detailNumber, extractTelephoneTarget(analysis.normalizedText, texts)),
    )

    QrContentType.Sms -> listOf(
        detailItem(Icons.Filled.Info, texts.detailDestination, extractSmsTarget(analysis.normalizedText, texts)),
        detailItem(Icons.Filled.Info, texts.detailMessage, extractSmsBody(analysis.normalizedText, texts)),
    )

    QrContentType.Wifi -> listOfNotNull(
        detailItem(Icons.Filled.Info, texts.detailNetwork, extractWifiField(analysis.normalizedText, "S", texts)),
        detailItem(Icons.Filled.Info, texts.detailSecurity, extractWifiSecurity(analysis.normalizedText, texts)),
        extractWifiPasswordRow(analysis.normalizedText, texts),
    )

    QrContentType.VCard -> listOf(
        detailItem(Icons.Filled.Info, texts.detailContact, texts.importContactAction),
    )

    QrContentType.Geo -> listOf(
        detailItem(Icons.Filled.Info, texts.detailLocation, texts.openMapAction),
    )

    QrContentType.Crypto -> listOf(
        detailItem(Icons.Filled.Info, texts.detailPayment, texts.cryptoAction),
    )

    QrContentType.PlainText -> listOf(
        detailItem(Icons.Filled.Description, texts.detailPlainText, analysis.normalizedText),
    )

    QrContentType.Unknown -> listOf(
        detailItem(Icons.Filled.Info, texts.detailUnknown, texts.notClassifiedPrecisely),
    )

    QrContentType.Url -> emptyList()
}

private fun detailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
): ResultDetailItem = ResultDetailItem(
    icon = icon,
    label = label,
    value = value,
    color = color,
)

internal fun contentTypeIcon(contentType: QrContentType): androidx.compose.ui.graphics.vector.ImageVector = when (contentType) {
    QrContentType.Url -> Icons.Filled.Language
    QrContentType.Email -> Icons.Filled.Email
    QrContentType.Phone -> Icons.Filled.Phone
    QrContentType.Sms -> Icons.Filled.Sms
    QrContentType.Wifi -> Icons.Filled.Wifi
    QrContentType.VCard -> Icons.Filled.Person
    QrContentType.Geo -> Icons.Filled.LocationOn
    QrContentType.Crypto -> Icons.Filled.AccountBalanceWallet
    QrContentType.PlainText -> Icons.Filled.Description
    QrContentType.Unknown -> Icons.AutoMirrored.Filled.HelpOutline
}

private fun actionIcon(contentType: QrContentType): androidx.compose.ui.graphics.vector.ImageVector = when (contentType) {
    QrContentType.Url -> Icons.Filled.OpenInBrowser
    QrContentType.Email -> Icons.Filled.Email
    QrContentType.Phone -> Icons.Filled.Phone
    QrContentType.Sms -> Icons.Filled.Sms
    QrContentType.Wifi -> Icons.Filled.Wifi
    QrContentType.VCard -> Icons.Filled.Person
    QrContentType.Geo -> Icons.Filled.LocationOn
    QrContentType.Crypto -> Icons.Filled.AccountBalanceWallet
    QrContentType.PlainText -> Icons.Filled.Description
    QrContentType.Unknown -> Icons.AutoMirrored.Filled.HelpOutline
}

private fun metadataIcon(label: String): androidx.compose.ui.graphics.vector.ImageVector = when {
    label == "Host" -> Icons.Filled.Language
    label == "Connection" -> Icons.Filled.Lock
    label == "Path" -> Icons.Filled.SubdirectoryArrowRight
    label == "Content" -> Icons.Filled.Description
    label == "Download" -> Icons.Filled.Download
    label == "Resolved destination" -> Icons.Filled.Link
    label == "S" -> Icons.Filled.Wifi
    label == "T" -> Icons.Filled.Lock
    label == "File name" -> Icons.Filled.Description
    label == "File type" -> Icons.Filled.Description
    label == "File extension" -> Icons.Filled.Description
    label == "Security key" -> Icons.Filled.Lock
    label == "SSID" -> Icons.Filled.Wifi
    else -> Icons.Filled.Info
}

private fun metadataDisplayLabel(
    label: String,
    texts: ResultTexts,
): String = when (label) {
    "Host" -> texts.detailHost
    "Connection" -> texts.detailScheme
    "Content" -> texts.detailContent
    "Resolved destination" -> texts.detailResolvedDestination
    "File name" -> texts.detailFileName
    "File type" -> texts.detailFileType
    "Download" -> texts.detailDownload
    "Path" -> texts.detailPath
    else -> label
}

private fun metadataDisplayValue(
    label: String,
    value: String,
    texts: ResultTexts,
): String = when {
    label == "Content" -> when (value) {
        "Web page" -> texts.webPageType
        "PDF document" -> texts.pdfDocumentType
        "Archive" -> texts.archiveType
        "Document" -> texts.fileTypeDocument
        "Spreadsheet" -> texts.fileTypeSpreadsheet
        "Presentation" -> texts.fileTypePresentation
        "Image" -> texts.fileTypeImage
        "Audio" -> texts.fileTypeAudio
        "Video" -> texts.fileTypeVideo
        "Android app" -> texts.fileTypeApk
        "File" -> texts.fileType
        "Unknown binary file" -> texts.unknownBinaryFileType
        else -> value
    }

    label == "Connection" -> when (value) {
        "HTTPS" -> texts.secureHttpsConnection
        "HTTP" -> texts.insecureHttpConnection
        else -> value
    }

    label == "Download" -> when (value) {
        "Downloadable file" -> texts.downloadableFile
        "Server suggests a file download" -> texts.serverSuggestsFileDownload
        else -> value
    }

    label == "File type" -> when (value) {
        "PDF" -> texts.fileTypePdf
        "ZIP" -> texts.fileTypeZip
        "RAR" -> texts.fileTypeRar
        "7z" -> texts.fileTypeSevenZip
        "Document" -> texts.fileTypeDocument
        "Spreadsheet" -> texts.fileTypeSpreadsheet
        "Presentation" -> texts.fileTypePresentation
        "Audio" -> texts.fileTypeAudio
        "Video" -> texts.fileTypeVideo
        "Image" -> texts.fileTypeImage
        "APK" -> texts.fileTypeApk
        "Android app" -> texts.fileTypeApk
        "File" -> texts.fileType
        else -> value
    }

    else -> value
}

internal fun extractMailTarget(
    text: String,
    texts: ResultTexts,
): String = text.removePrefix("mailto:").substringBefore('?').ifBlank {
    texts.noValue
}

internal fun extractMailExtras(
    text: String,
    texts: ResultTexts,
): String {
    val query = text.substringAfter('?', "")
    return when {
        query.isBlank() -> texts.noSubjectOrBody
        query.contains("subject=") || query.contains("body=") -> texts.predefinedFields
        else -> texts.parametersPresent
    }
}

internal fun extractTelephoneTarget(
    text: String,
    texts: ResultTexts,
): String = text.removePrefix("tel:").ifBlank { texts.noValue }

internal fun extractSmsTarget(
    text: String,
    texts: ResultTexts,
): String = text.removePrefix("sms:").substringBefore('?').ifBlank {
    texts.noValue
}

internal fun extractSmsBody(
    text: String,
    texts: ResultTexts,
): String {
    val body = text.substringAfter("body=", "")
    return if (body.isBlank()) texts.noPredefinedMessage else body.substringBefore('&')
}

internal fun extractWifiField(
    text: String,
    key: String,
    texts: ResultTexts,
): String {
    val payload = text.removePrefix("WIFI:")
    return payload
        .split(';')
        .asSequence()
        .mapNotNull { entry ->
            val separatorIndex = entry.indexOf(':')
            if (separatorIndex <= 0) return@mapNotNull null
            val entryKey = entry.substring(0, separatorIndex).uppercase()
            val entryValue = entry.substring(separatorIndex + 1)
            if (entryKey == key) entryValue else null
        }.firstOrNull()
        .orEmpty()
        .ifBlank { texts.noValue }
}

internal fun extractWifiSecurity(
    text: String,
    texts: ResultTexts,
): String {
    val security = extractWifiField(text, "T", texts)
    return when (security.lowercase()) {
        "nopass" -> texts.openNetwork
        texts.noValue.lowercase() -> texts.noValue
        else -> security
    }
}

internal fun extractWifiPasswordRow(
    text: String,
    texts: ResultTexts,
): ResultDetailItem? {
    val payload = text.removePrefix("WIFI:")
    val hasPassword =
        payload
            .split(';')
            .asSequence()
            .any { entry ->
                entry.startsWith("P:", ignoreCase = true) && entry.substringAfter(':').isNotBlank()
            }

    return if (hasPassword) {
        detailItem(Icons.Filled.Info, texts.detailSecurityKey, texts.present)
    } else {
        null
    }
}

internal fun contentTypeLabel(
    contentType: QrContentType,
    texts: ResultTexts,
): String = when (contentType) {
    QrContentType.Url -> texts.urlLabel
    QrContentType.Email -> texts.detailContact
    QrContentType.Phone -> texts.phoneType
    QrContentType.Sms -> texts.smsType
    QrContentType.Wifi -> texts.wifiType
    QrContentType.VCard -> texts.vCardType
    QrContentType.Geo -> texts.locationType
    QrContentType.Crypto -> texts.cryptoType
    QrContentType.PlainText -> texts.plainTextType
    QrContentType.Unknown -> texts.unknownType
}

internal fun contentActionLabel(
    contentType: QrContentType,
    openableUrl: String?,
    texts: ResultTexts,
): String = when (contentType) {
    QrContentType.Url -> if (openableUrl.isNullOrBlank()) texts.notALink else texts.openLink
    QrContentType.Email -> texts.emailAction
    QrContentType.Phone -> texts.phoneAction
    QrContentType.Sms -> texts.smsAction
    QrContentType.Wifi -> texts.wifiAction
    QrContentType.VCard -> texts.importContactAction
    QrContentType.Geo -> texts.openMapAction
    QrContentType.Crypto -> texts.cryptoAction
    QrContentType.PlainText -> texts.notALink
    QrContentType.Unknown -> texts.notClassifiedPrecisely
}

internal fun urlActionLabel(
    analysis: QrAnalysisResult,
    texts: ResultTexts,
): String {
    val contentValue = metadataValue(analysis.localScan.metadata, "Content")
    val fileTypeValue = metadataValue(analysis.localScan.metadata, "File type")

    return when {
        contentValue == texts.webPageType -> texts.openLink
        contentValue == texts.unknownBinaryFileType -> texts.downloadFileCaution
        contentValue == texts.archiveType || fileTypeValue == texts.fileTypeZip || fileTypeValue == texts.fileTypeRar || fileTypeValue == texts.fileTypeSevenZip -> texts.downloadFile
        contentValue == texts.fileTypeApk ||
            fileTypeValue == texts.fileTypeApk ||
            fileTypeValue.orEmpty().contains("disk image", ignoreCase = true) ||
            fileTypeValue.orEmpty().contains("executable", ignoreCase = true) ||
            fileTypeValue.orEmpty().contains("script", ignoreCase = true) -> texts.downloadAppFileCaution
        contentValue == texts.pdfDocumentType || fileTypeValue == texts.fileTypePdf || fileTypeValue == texts.fileTypeDocument || fileTypeValue == texts.fileTypeSpreadsheet || fileTypeValue == texts.fileTypePresentation -> texts.openDocument
        contentValue == texts.fileTypeImage || fileTypeValue == texts.fileTypeImage -> texts.openImage
        contentValue == texts.fileTypeAudio || contentValue == texts.fileTypeVideo || fileTypeValue == texts.fileTypeAudio || fileTypeValue == texts.fileTypeVideo -> texts.openMedia
        fileTypeValue == texts.fileType || contentValue == texts.fileType -> texts.openFile
        else -> if (analysis.openableUrl.isNullOrBlank()) texts.notALink else texts.openLink
    }
}

private fun metadataValue(
    metadata: List<com.lmartin.qrguardian.domain.model.ScanMetadataItem>,
    label: String,
): String? = metadata.firstOrNull { it.label == label }?.value

internal fun localScanSummary(
    section: ScanSectionResult,
    texts: ResultTexts,
): String = texts.levelDescription(section.level)

internal fun localScanBadgeLabel(
    section: ScanSectionResult,
    texts: ResultTexts,
): String = when (section.level) {
    SecurityLevel.Safe -> texts.statusSafe
    SecurityLevel.Suspicious -> texts.statusCareful
    SecurityLevel.Dangerous -> texts.statusDangerous
    SecurityLevel.Unknown -> texts.statusUnknown
}

internal fun remoteScanBadgeLabel(
    section: ScanSectionResult,
    texts: ResultTexts,
): String = when (section.status) {
    ScanStatus.Completed -> texts.remoteCompleted
    ScanStatus.NotConfigured -> texts.remoteShortNotConfigured
    ScanStatus.NotApplicable -> texts.remoteShortNotApplicable
    ScanStatus.Unavailable -> texts.remoteShortUnavailable
}

internal fun contentAccentColor(contentType: QrContentType): androidx.compose.ui.graphics.Color = when (contentType) {
    QrContentType.Url -> QrGuardianColors.PrimaryDark
    QrContentType.Email -> QrGuardianColors.PrimaryDark
    QrContentType.Phone -> QrGuardianColors.PrimaryDark
    QrContentType.Sms -> QrGuardianColors.PrimaryDark
    QrContentType.Wifi -> QrGuardianColors.PrimaryDark
    QrContentType.VCard -> QrGuardianColors.PrimaryDark
    QrContentType.Geo -> QrGuardianColors.PrimaryDark
    QrContentType.Crypto -> QrGuardianColors.PrimaryDark
    QrContentType.PlainText -> QrGuardianColors.PrimaryDark
    QrContentType.Unknown -> QrGuardianColors.PrimaryDark
}
