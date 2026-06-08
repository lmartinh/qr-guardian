package com.lmartin.qrguardian.presentation.result

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanMetadataItem
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.theme.QrGuardianTheme

@Preview(name = "Safe result")
@Composable
private fun ResultScreenPreviewSafe() {
    QrGuardianTheme {
        ResultScreen(
            viewModel = ResultViewModel.preview(ResultUiState.success(sampleUrlResult(SecurityLevel.Safe))),
            onOpenUrl = {},
            onRescanClick = {},
        )
    }
}

@Preview(name = "Suspicious result", showBackground = true)
@Composable
private fun ResultScreenPreviewSuspicious() {
    QrGuardianTheme {
        ResultScreen(
            viewModel = ResultViewModel.preview(ResultUiState.success(sampleUrlResult(SecurityLevel.Suspicious))),
            onOpenUrl = {},
            onRescanClick = {},
        )
    }
}

@Preview(name = "No remote checks", showBackground = true)
@Composable
private fun ResultScreenPreviewNoRemote() {
    QrGuardianTheme {
        ResultScreen(
            viewModel = ResultViewModel.preview(ResultUiState.success(sampleNoRemoteResult())),
            onOpenUrl = {},
            onRescanClick = {},
        )
    }
}

@Preview(name = "PDF download", showBackground = true)
@Composable
private fun ResultScreenPreviewPdfDownload() {
    QrGuardianTheme {
        ResultScreen(
            viewModel = ResultViewModel.preview(ResultUiState.success(samplePdfDownloadResult())),
            onOpenUrl = {},
            onRescanClick = {},
        )
    }
}

@Preview(name = "Mailto", showBackground = true)
@Composable
private fun ResultScreenPreviewMail() {
    QrGuardianTheme {
        ResultScreen(
            viewModel = ResultViewModel.preview(ResultUiState.success(sampleMailResult())),
            onOpenUrl = {},
            onRescanClick = {},
        )
    }
}

@Preview(name = "Plain text", showBackground = true)
@Composable
private fun ResultScreenPreviewPlainText() {
    QrGuardianTheme {
        ResultScreen(
            viewModel = ResultViewModel.preview(ResultUiState.success(samplePlainTextResult())),
            onOpenUrl = {},
            onRescanClick = {},
        )
    }
}

private fun sampleUrlResult(level: SecurityLevel): QrAnalysisResult = QrAnalysisResult(
    originalText = "https://secure-login.example.com/account",
    normalizedText = "https://secure-login.example.com/account",
    openableUrl = "https://secure-login.example.com/account",
    contentType = QrContentType.Url,
    overallLevel = level,
    canOpen = true,
    localScan = ScanSectionResult(
        name = "Local scan",
        level = level,
        status = ScanStatus.Completed,
        title = when (level) {
            SecurityLevel.Safe -> "No local red flags"
            SecurityLevel.Suspicious -> "Multiple suspicious signals"
            SecurityLevel.Dangerous -> "High-risk local indicators"
            SecurityLevel.Unknown -> "Local scan incomplete"
        },
        description = when (level) {
            SecurityLevel.Safe -> "The QR content looks consistent and contains no obvious local red flags."
            SecurityLevel.Suspicious -> "The QR content includes patterns that deserve a second look before opening."
            SecurityLevel.Dangerous -> "The QR content contains high-risk indicators such as brand impersonation or dangerous schemes."
            SecurityLevel.Unknown -> "The QR content could not be fully analyzed on device."
        },
        reasons = when (level) {
            SecurityLevel.Safe -> listOf("HTTPS is present.", "The host name looks stable.", "No suspicious keywords were found.")
            SecurityLevel.Suspicious -> listOf("The domain resembles a login flow.", "The path suggests credential collection.", "The content asks for user action.")
            SecurityLevel.Dangerous -> listOf("The URL uses a risky pattern.", "The destination looks impersonated.", "High-risk indicators were matched.")
            SecurityLevel.Unknown -> listOf("Not enough data was available to complete the local scan.")
        },
        metadata = listOf(
            ScanMetadataItem(label = "Type", value = "URL"),
        ),
    ),
    remoteReputation = ScanSectionResult(
        name = "Remote reputation",
        level = when (level) {
            SecurityLevel.Safe -> SecurityLevel.Safe
            SecurityLevel.Suspicious -> SecurityLevel.Suspicious
            SecurityLevel.Dangerous -> SecurityLevel.Dangerous
            SecurityLevel.Unknown -> SecurityLevel.Unknown
        },
        status = ScanStatus.Completed,
        title = when (level) {
            SecurityLevel.Safe -> "No provider hits"
            SecurityLevel.Suspicious -> "Mixed reputation signals"
            SecurityLevel.Dangerous -> "Malicious verdict"
            SecurityLevel.Unknown -> "Reputation unavailable"
        },
        description = when (level) {
            SecurityLevel.Safe -> "The remote reputation sources did not return a blocking signal."
            SecurityLevel.Suspicious -> "The reputation sources are not unanimous, so caution is recommended."
            SecurityLevel.Dangerous -> "Remote checks point to a malicious or clearly unsafe destination."
            SecurityLevel.Unknown -> "No remote result was available for this sample."
        },
        reasons = when (level) {
            SecurityLevel.Safe -> listOf("No provider flags were returned.", "The destination was not found on deny lists.")
            SecurityLevel.Suspicious -> listOf("One provider returned a cautionary signal.", "The destination is not strongly trusted.")
            SecurityLevel.Dangerous -> listOf("A provider returned a malicious verdict.", "The destination should not be opened.")
            SecurityLevel.Unknown -> listOf("Remote reputation could not be checked.")
        },
        metadata = listOf(
            ScanMetadataItem(label = "Provider", value = "Backend proxy"),
            ScanMetadataItem(label = "Checked", value = "Just now"),
        ),
    ),
)

private fun sampleNoRemoteResult(): QrAnalysisResult = sampleUrlResult(SecurityLevel.Safe).copy(
    remoteReputation = ScanSectionResult(
        name = "Remote reputation",
        level = SecurityLevel.Unknown,
        status = ScanStatus.NotConfigured,
        title = "Remote reputation is not configured",
        description = "This build does not have remote reputation checks enabled.",
        reasons = emptyList(),
        metadata = listOf(
            ScanMetadataItem(label = "Provider", value = "Not configured"),
            ScanMetadataItem(label = "Last check", value = "Not available"),
        ),
    ),
)

private fun samplePdfDownloadResult(): QrAnalysisResult = QrAnalysisResult(
    originalText = "https://cdn.example.com/files/security-guide.pdf",
    normalizedText = "https://cdn.example.com/files/security-guide.pdf",
    openableUrl = "https://cdn.example.com/files/security-guide.pdf",
    contentType = QrContentType.Url,
    overallLevel = SecurityLevel.Safe,
    canOpen = true,
    localScan = ScanSectionResult(
        name = "Local scan",
        level = SecurityLevel.Safe,
        status = ScanStatus.Completed,
        title = "Direct download looks consistent",
        description = "The URL points to a PDF file and does not show suspicious local indicators.",
        reasons = listOf(
            "The host is stable.",
            "The protocol is HTTPS.",
        ),
        metadata = listOf(
            ScanMetadataItem(label = "Type", value = "URL"),
        ),
    ),
    remoteReputation = ScanSectionResult(
        name = "Remote reputation",
        level = SecurityLevel.Unknown,
        status = ScanStatus.NotConfigured,
        title = "Remote reputation not configured",
        description = "Remote verification is not configured in this preview.",
        reasons = emptyList(),
        metadata = listOf(
            ScanMetadataItem(label = "Provider", value = "Not configured"),
        ),
    ),
)

private fun sampleMailResult(): QrAnalysisResult = QrAnalysisResult(
    originalText = "mailto:hello@company.com?subject=Support&body=I%20need%20help",
    normalizedText = "mailto:hello@company.com?subject=Support&body=I%20need%20help",
    openableUrl = null,
    contentType = QrContentType.Email,
    overallLevel = SecurityLevel.Suspicious,
    canOpen = true,
    localScan = ScanSectionResult(
        name = "Local scan",
        level = SecurityLevel.Suspicious,
        status = ScanStatus.Completed,
        title = "Email action detected",
        description = "This QR starts an email composition flow, which should be reviewed before opening.",
        reasons = listOf(
            "The mailto link pre-fills a subject.",
            "The mailto link includes a prewritten body.",
        ),
        metadata = listOf(
            ScanMetadataItem(label = "Type", value = "Email"),
        ),
    ),
    remoteReputation = ScanSectionResult(
        name = "Remote reputation",
        level = SecurityLevel.Unknown,
        status = ScanStatus.NotApplicable,
        title = "Remote reputation not applicable",
        description = "Email actions do not use remote reputation checks.",
        reasons = emptyList(),
        metadata = emptyList(),
    ),
)

private fun samplePlainTextResult(): QrAnalysisResult = QrAnalysisResult(
    originalText = "QR Guardian notes",
    normalizedText = "QR Guardian notes",
    openableUrl = null,
    contentType = QrContentType.PlainText,
    overallLevel = SecurityLevel.Unknown,
    canOpen = false,
    localScan = ScanSectionResult(
        name = "Local scan",
        level = SecurityLevel.Unknown,
        status = ScanStatus.Completed,
        title = "Plain text detected",
        description = "The QR contains a text string rather than an action or URL.",
        reasons = listOf(
            "This content is not a link.",
            "No remote reputation check is required.",
        ),
        metadata = listOf(
            ScanMetadataItem(label = "Type", value = "Plain text"),
        ),
    ),
    remoteReputation = ScanSectionResult(
        name = "Remote reputation",
        level = SecurityLevel.Unknown,
        status = ScanStatus.NotApplicable,
        title = "Not applicable",
        description = "Plain text does not need reputation checks.",
        reasons = emptyList(),
        metadata = emptyList(),
    ),
)
