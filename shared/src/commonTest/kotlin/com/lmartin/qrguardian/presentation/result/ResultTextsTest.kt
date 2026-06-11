package com.lmartin.qrguardian.presentation.result

import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultTextsTest {
    @Test
    fun `security level titles descriptions and section summaries map correctly`() {
        val texts = fakeTexts()

        assertEquals("Looks safe", texts.levelTitle(SecurityLevel.Safe))
        assertEquals("Be careful", texts.levelTitle(SecurityLevel.Suspicious))
        assertEquals("Potentially dangerous", texts.levelTitle(SecurityLevel.Dangerous))
        assertEquals("Unknown content", texts.levelTitle(SecurityLevel.Unknown))

        assertEquals("No suspicious signals were detected.", texts.levelDescription(SecurityLevel.Safe))
        assertEquals("This QR code contains signals that should be reviewed before opening.", texts.levelDescription(SecurityLevel.Suspicious))
        assertEquals("This QR code contains high-risk signals. Opening it is not recommended.", texts.levelDescription(SecurityLevel.Dangerous))
        assertEquals("This QR code could not be fully evaluated.", texts.levelDescription(SecurityLevel.Unknown))

        assertEquals(
            "Unavailable",
            texts.sectionSummary(section(ScanStatus.NotConfigured, SecurityLevel.Unknown)),
        )
        assertEquals("Not applicable", texts.sectionSummary(section(ScanStatus.NotApplicable, SecurityLevel.Unknown)))
        assertEquals("Unavailable", texts.sectionSummary(section(ScanStatus.Unavailable, SecurityLevel.Unknown)))
        assertEquals("This QR code contains high-risk signals. Opening it is not recommended.", texts.sectionSummary(section(ScanStatus.Completed, SecurityLevel.Dangerous)))
    }

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

    private fun section(
        status: ScanStatus,
        level: SecurityLevel,
    ): ScanSectionResult = ScanSectionResult(
        name = "Remote Reputation",
        level = level,
        status = status,
        title = level.title(),
        description = level.description(),
        reasons = emptyList(),
    )
}
