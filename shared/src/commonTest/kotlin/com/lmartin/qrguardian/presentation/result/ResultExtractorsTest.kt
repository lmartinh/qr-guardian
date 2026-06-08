package com.lmartin.qrguardian.presentation.result

import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanMetadataItem
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResultExtractorsTest {
    @Test
    fun `url details are ordered and do not infer file rows from normalized text`() {
        val analysis = analysis(
            contentType = QrContentType.Url,
            normalizedText = "https://example.com",
            metadata =
            listOf(
                ScanMetadataItem(label = "Host", value = "example.com"),
                ScanMetadataItem(label = "Connection", value = "HTTPS"),
            ),
        )

        val details = buildLocalAnalysisDetails(analysis, fakeTexts())

        assertEquals(4, details.size)
        assertEquals("Type", details[0].label)
        assertEquals("Action", details[1].label)
        assertEquals("Host", details[2].label)
        assertEquals("Connection", details[3].label)
        assertEquals("Secure HTTPS", details[3].value)
        assertTrue(
            details.none { item ->
                item.label == "File name" || item.label == "File extension" || item.label == "File type" || item.label == "Download"
            },
        )
    }

    @Test
    fun `http connection is shown as insecure`() {
        val analysis = analysis(
            contentType = QrContentType.Url,
            normalizedText = "http://example.com/login",
            metadata =
            listOf(
                ScanMetadataItem(label = "Host", value = "example.com"),
                ScanMetadataItem(label = "Connection", value = "HTTP"),
            ),
        )

        val details = buildLocalAnalysisDetails(analysis, fakeTexts())

        assertTrue(details.any { it.label == "Connection" && it.value == "Insecure HTTP" })
    }

    @Test
    fun `sms details show a message preview without exposing the raw query`() {
        val analysis = analysis(
            contentType = QrContentType.Sms,
            normalizedText = "sms:+34123456789?body=HELLO",
        )

        val details = buildLocalAnalysisDetails(analysis, fakeTexts())

        assertEquals("Type", details[0].label)
        assertEquals("Action", details[1].label)
        assertEquals("Recipient", details[2].label)
        assertEquals("Message preview", details[3].label)
        assertEquals("+34123456789", details[2].value)
        assertEquals("HELLO", details[3].value)
    }

    @Test
    fun `wifi details never expose the password value`() {
        val analysis = analysis(
            contentType = QrContentType.Wifi,
            normalizedText = "WIFI:T:WPA;S:MyWifi;P:secret;;",
        )

        val details = buildLocalAnalysisDetails(analysis, fakeTexts())

        assertTrue(details.any { it.label == "Network name" && it.value == "MyWifi" })
        assertTrue(details.any { it.label == "Security" && it.value == "WPA" })
        assertTrue(details.any { it.label == "Password" && it.value == "Present" })
        assertTrue(details.none { it.value == "secret" })
    }

    private fun analysis(
        contentType: QrContentType,
        normalizedText: String,
        metadata: List<ScanMetadataItem> = emptyList(),
    ): QrAnalysisResult = QrAnalysisResult(
        originalText = normalizedText,
        normalizedText = normalizedText,
        openableUrl = normalizedText,
        contentType = contentType,
        overallLevel = SecurityLevel.Safe,
        canOpen = true,
        localScan = ScanSectionResult(
            name = "Local Scan",
            level = SecurityLevel.Safe,
            status = ScanStatus.Completed,
            title = SecurityLevel.Safe.title(),
            description = SecurityLevel.Safe.description(),
            reasons = emptyList(),
            metadata = metadata,
        ),
        remoteReputation = ScanSectionResult(
            name = "Remote Reputation",
            level = SecurityLevel.Unknown,
            status = ScanStatus.NotApplicable,
            title = SecurityLevel.Unknown.title(),
            description = "Only URLs are checked against remote reputation providers.",
            reasons = emptyList(),
            metadata = emptyList(),
        ),
    )

    private fun fakeTexts(): ResultTexts = ResultTexts(
        title = "Title",
        openLink = "Open link",
        rescan = "Scan again",
        localScan = "Local scan",
        localSignals = "Local signals",
        remoteReputation = "Remote reputation",
        statusRecommended = "Recommended",
        statusReview = "Review",
        statusBlocked = "Blocked",
        statusUncertain = "Uncertain",
        statusSafe = "Safe",
        statusCareful = "Careful",
        statusDangerous = "Dangerous",
        statusUnknown = "Unknown",
        levelSafeTitle = "Looks safe",
        levelSafeDescription = "No suspicious signals were detected.",
        levelSuspiciousTitle = "Be careful",
        levelSuspiciousDescription = "This QR code contains signals that should be reviewed before opening.",
        levelDangerousTitle = "Potentially dangerous",
        levelDangerousDescription = "This QR code contains high-risk signals. Opening it is not recommended.",
        levelUnknownTitle = "Unknown content",
        levelUnknownDescription = "This QR code could not be fully evaluated.",
        qrContentLabel = "QR Content",
        urlLabel = "URL",
        errorTitle = "Error",
        idleTitle = "Idle",
        idleMessage = "Idle",
        loadingTitle = "Loading",
        loadingMessage = "Loading",
        loadingAction = "Please wait",
        detailType = "Type",
        detailAction = "Action",
        detailHost = "Host",
        detailScheme = "Connection",
        detailContent = "Content",
        detailDestination = "Recipient",
        detailResolvedDestination = "Resolved destination",
        detailExtraField = "Extra field",
        detailNumber = "Number",
        detailMessage = "Message preview",
        detailSecurity = "Security",
        detailNetwork = "Network name",
        detailSsid = "SSID",
        detailSsidKey = "SSID key",
        detailSecurityKey = "Password",
        detailContact = "Email address",
        detailLocation = "Location",
        detailPayment = "Payment",
        detailPlainText = "Preview",
        detailUnknown = "Unknown",
        detailUsage = "Usage",
        detailState = "State",
        detailFileName = "File name",
        detailFileExtension = "File extension",
        detailFileType = "File type",
        detailPath = "Path",
        detailDownload = "Download",
        noValue = "Not specified",
        noSubjectOrBody = "No subject or body",
        predefinedFields = "Includes predefined fields",
        parametersPresent = "Parameters present",
        noPredefinedMessage = "No predefined message",
        notClassifiedPrecisely = "Could not be classified precisely",
        notALink = "No automatic action",
        emailAction = "Compose email",
        phoneType = "Phone number",
        phoneAction = "Call number",
        smsType = "SMS",
        smsAction = "Send SMS",
        wifiType = "Wi‑Fi",
        wifiAction = "Join Wi‑Fi network",
        vCardType = "Contact / vCard",
        importContactAction = "Import contact",
        locationType = "Location",
        openMapAction = "Open map",
        cryptoType = "Crypto payment",
        cryptoAction = "Open wallet",
        plainTextType = "Plain text",
        unknownType = "Unknown",
        webPageType = "Web page",
        secureHttpsConnection = "Secure HTTPS",
        insecureHttpConnection = "Insecure HTTP",
        pdfDocumentType = "PDF document",
        archiveType = "Archive",
        fileType = "File",
        unknownBinaryFileType = "Unknown binary file",
        downloadableFile = "File link detected",
        serverSuggestsFileDownload = "Server suggests a file download",
        redirectedTo = "Resolved destination",
        openNetwork = "Open network",
        present = "Present",
        fileTypeApk = "Android app",
        fileTypePdf = "PDF",
        fileTypeZip = "ZIP",
        fileTypeRar = "RAR",
        fileTypeSevenZip = "7z",
        fileTypeDocument = "Document",
        fileTypeSpreadsheet = "Spreadsheet",
        fileTypePresentation = "Presentation",
        fileTypeAudio = "Audio",
        fileTypeVideo = "Video",
        fileTypeImage = "Image",
        remoteCompleted = "Remote check completed",
        remoteNotConfigured = "Unavailable",
        remoteNotApplicable = "Not applicable",
        remoteUnavailable = "Unavailable",
        remoteShortNotConfigured = "Not configured",
        remoteShortNotApplicable = "Not applicable",
        remoteShortUnavailable = "Unavailable",
    )
}
