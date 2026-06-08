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
    fun `pdf url shows open document action`() {
        val analysis = analysis(
            contentType = QrContentType.Url,
            normalizedText = "https://example.com/menu.pdf",
            metadata =
            listOf(
                ScanMetadataItem(label = "Host", value = "example.com"),
                ScanMetadataItem(label = "Connection", value = "HTTPS"),
                ScanMetadataItem(label = "Content", value = "PDF document"),
                ScanMetadataItem(label = "File name", value = "menu.pdf"),
                ScanMetadataItem(label = "File type", value = "PDF"),
            ),
        )

        val details = buildLocalAnalysisDetails(analysis, fakeTexts())

        assertEquals("Open document", details[1].value)
        assertTrue(details.any { it.label == "File name" && it.value == "menu.pdf" })
        assertTrue(details.none { it.label == "Path" })
    }

    @Test
    fun `archive url shows download file action`() {
        val analysis = analysis(
            contentType = QrContentType.Url,
            normalizedText = "https://example.com/archive.zip",
            metadata =
            listOf(
                ScanMetadataItem(label = "Host", value = "example.com"),
                ScanMetadataItem(label = "Connection", value = "HTTPS"),
                ScanMetadataItem(label = "Content", value = "Archive"),
                ScanMetadataItem(label = "File name", value = "archive.zip"),
                ScanMetadataItem(label = "File type", value = "Archive"),
                ScanMetadataItem(label = "Download", value = "Direct file link detected"),
            ),
        )

        val details = buildLocalAnalysisDetails(analysis, fakeTexts())

        assertEquals("Download file", details[1].value)
        assertTrue(details.any { it.label == "Download" && it.value == "Direct file link detected" })
    }

    @Test
    fun `unknown binary url shows caution download action`() {
        val analysis = analysis(
            contentType = QrContentType.Url,
            normalizedText = "https://example.com/file",
            metadata =
            listOf(
                ScanMetadataItem(label = "Host", value = "example.com"),
                ScanMetadataItem(label = "Connection", value = "HTTPS"),
                ScanMetadataItem(label = "Content", value = "Unknown binary file"),
                ScanMetadataItem(label = "Download", value = "Direct file link detected"),
            ),
        )

        val details = buildLocalAnalysisDetails(analysis, fakeTexts())

        assertEquals("Download file with caution", details[1].value)
    }

    @Test
    fun `blocked scheme shows blocked unknown content without open link row`() {
        val analysis = analysis(
            contentType = QrContentType.Unknown,
            normalizedText = "javascript:alert(1)",
            openableUrl = null,
            overallLevel = SecurityLevel.Dangerous,
            canOpen = false,
            localReasons = listOf("""The scanned content uses the blocked scheme "javascript"."""),
        )

        val details = buildLocalAnalysisDetails(analysis, fakeTexts())
        val localSignals = buildLocalSignals(analysis.localScan)

        assertEquals("Unknown", details[0].value)
        assertEquals("Could not be classified precisely", details[1].value)
        assertTrue(details.none { it.label == "Action" && it.value == "Open link" })
        assertTrue(localSignals.any { it.contains("blocked scheme") })
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
        openableUrl: String? = normalizedText,
        overallLevel: SecurityLevel = SecurityLevel.Safe,
        canOpen: Boolean = true,
        localReasons: List<String> = emptyList(),
        metadata: List<ScanMetadataItem> = emptyList(),
    ): QrAnalysisResult = QrAnalysisResult(
        originalText = normalizedText,
        normalizedText = normalizedText,
        openableUrl = openableUrl,
        contentType = contentType,
        overallLevel = overallLevel,
        canOpen = canOpen,
        localScan = ScanSectionResult(
            name = "Local Scan",
            level = overallLevel,
            status = ScanStatus.Completed,
            title = overallLevel.title(),
            description = overallLevel.description(),
            reasons = localReasons,
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
        openDocument = "Open document",
        openImage = "Open image",
        openMedia = "Open audio/video",
        openLink = "Open link",
        openFile = "Open file",
        downloadFile = "Download file",
        downloadFileCaution = "Download file with caution",
        downloadAppFileCaution = "Download app or file with caution",
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
        downloadableFile = "Direct file link detected",
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
