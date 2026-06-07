package com.lmartin.qrguardian.presentation.result

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel

internal data class ResultDetailItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val color: Color,
)

internal fun buildLocalAnalysisDetails(
    analysis: QrAnalysisResult,
    texts: ResultTexts,
): List<ResultDetailItem> {
    val items = mutableListOf<ResultDetailItem>()

    items += detailItem(
        icon = contentTypeIcon(analysis.contentType),
        label = texts.detailType,
        value = contentTypeLabel(analysis.contentType, texts),
    )
    items += detailItem(
        icon = actionIcon(analysis.contentType),
        label = texts.detailAction,
        value = contentActionLabel(analysis.contentType, analysis.openableUrl, texts),
    )

    when (analysis.contentType) {
        QrContentType.Url -> {
            val downloadType = detectDownloadType(analysis.normalizedText, texts)
            val path = extractUrlPath(analysis.normalizedText)
            if (downloadType.isNotBlank()) {
                items += detailItem(Icons.Filled.Description, texts.detailFile, downloadType)
            }
            if (path.isNotBlank()) {
                items += detailItem(Icons.Filled.SubdirectoryArrowRight, texts.detailPath, path)
            }
        }
        QrContentType.Email -> {
            items += detailItem(Icons.Filled.Email, texts.detailDestination, extractMailTarget(analysis.normalizedText, texts))
            items += detailItem(Icons.Filled.Info, texts.detailExtraField, extractMailExtras(analysis.normalizedText, texts))
        }
        QrContentType.Phone -> {
            items += detailItem(Icons.Filled.Phone, texts.detailNumber, extractTelephoneTarget(analysis.normalizedText, texts))
        }
        QrContentType.Sms -> {
            items += detailItem(Icons.Filled.Sms, texts.detailDestination, extractSmsTarget(analysis.normalizedText, texts))
            items += detailItem(Icons.Filled.Sms, texts.detailMessage, extractSmsBody(analysis.normalizedText, texts))
        }
        QrContentType.Wifi -> {
            items += detailItem(Icons.Filled.Wifi, texts.detailNetwork, extractWifiField(analysis.normalizedText, "S", texts))
            items += detailItem(Icons.Filled.Info, texts.detailSecurity, extractWifiField(analysis.normalizedText, "T", texts))
        }
        QrContentType.VCard -> {
            items += detailItem(Icons.Filled.Person, texts.detailContact, texts.importContactAction)
        }
        QrContentType.Geo -> {
            items += detailItem(Icons.Filled.LocationOn, texts.detailLocation, texts.openMapAction)
        }
        QrContentType.Crypto -> {
            items += detailItem(Icons.Filled.AccountBalanceWallet, texts.detailPayment, texts.cryptoAction)
        }
        QrContentType.PlainText -> {
            items += detailItem(Icons.Filled.TextSnippet, texts.detailPlainText, analysis.normalizedText)
        }
        QrContentType.Unknown -> {
            items += detailItem(Icons.Filled.Info, texts.detailUnknown, texts.notClassifiedPrecisely)
        }
    }

    analysis.localScan.metadata.forEach { metadata ->
        items += detailItem(
            icon = metadataIcon(metadata.label),
            label = metadata.label,
            value = metadata.value,
        )
    }

    return items
}

internal fun buildLocalSignals(
    section: ScanSectionResult,
): List<String> {
    return section.reasons
}

internal fun buildRemoteAnalysisDetails(
    section: ScanSectionResult,
    texts: ResultTexts,
): List<ResultDetailItem> {
    val items = mutableListOf<ResultDetailItem>()

    items += detailItem(
        icon = when (section.status) {
            ScanStatus.Completed -> Icons.Filled.CheckCircle
            ScanStatus.NotConfigured -> Icons.Filled.Warning
            ScanStatus.NotApplicable -> Icons.Filled.Warning
            ScanStatus.Unavailable -> Icons.Filled.Warning
        },
        label = texts.detailState,
        value = section.statusLabel(texts),
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

internal fun SecurityLevel.toSignalColor(): Color {
    return when (this) {
        SecurityLevel.Safe -> Color(0xFF10B981)
        SecurityLevel.Suspicious -> Color(0xFF9A6B00)
        SecurityLevel.Dangerous -> Color(0xFFB42318)
        SecurityLevel.Unknown -> Color(0xFF6B7280)
    }
}

private fun detailItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color = Color.Unspecified,
): ResultDetailItem {
    return ResultDetailItem(
        icon = icon,
        label = label,
        value = value,
        color = color,
    )
}

private fun contentTypeIcon(contentType: QrContentType): ImageVector {
    return when (contentType) {
        QrContentType.Url -> Icons.Filled.TextSnippet
        QrContentType.Email -> Icons.Filled.Email
        QrContentType.Phone -> Icons.Filled.Phone
        QrContentType.Sms -> Icons.Filled.Sms
        QrContentType.Wifi -> Icons.Filled.Wifi
        QrContentType.VCard -> Icons.Filled.Person
        QrContentType.Geo -> Icons.Filled.LocationOn
        QrContentType.Crypto -> Icons.Filled.AccountBalanceWallet
        QrContentType.PlainText -> Icons.Filled.TextSnippet
        QrContentType.Unknown -> Icons.Filled.Info
    }
}

private fun actionIcon(contentType: QrContentType): ImageVector {
    return when (contentType) {
        QrContentType.Url -> Icons.Filled.Launch
        QrContentType.Email -> Icons.Filled.Email
        QrContentType.Phone -> Icons.Filled.Phone
        QrContentType.Sms -> Icons.Filled.Sms
        QrContentType.Wifi -> Icons.Filled.Wifi
        QrContentType.VCard -> Icons.Filled.Person
        QrContentType.Geo -> Icons.Filled.Launch
        QrContentType.Crypto -> Icons.Filled.AccountBalanceWallet
        QrContentType.PlainText -> Icons.Filled.TextSnippet
        QrContentType.Unknown -> Icons.Filled.Info
    }
}

private fun metadataIcon(label: String): ImageVector {
    return when {
        label == "S" -> Icons.Filled.Wifi
        label == "T" -> Icons.Filled.Info
        else -> Icons.Filled.Info
    }
}

private fun ScanSectionResult.statusLabel(texts: ResultTexts): String {
    return when (status) {
        ScanStatus.Completed -> texts.remoteCompleted
        ScanStatus.NotConfigured -> texts.remoteNotConfiguredStatus
        ScanStatus.NotApplicable -> texts.remoteNotApplicableStatus
        ScanStatus.Unavailable -> texts.remoteUnavailableStatus
    }
}
