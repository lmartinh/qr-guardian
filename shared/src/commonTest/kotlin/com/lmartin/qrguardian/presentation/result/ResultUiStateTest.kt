package com.lmartin.qrguardian.presentation.result

import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class ResultUiStateTest {
    @Test
    fun `url analyses gate the open button and expose openable urls`() {
        val safeHttpsState = ResultUiState.success(
            analysis = analysis(
                contentType = QrContentType.Url,
                overallLevel = SecurityLevel.Safe,
                canOpen = true,
                openableUrl = "https://example.com"
            )
        )
        val bareDomainState = ResultUiState.success(
            analysis = analysis(
                contentType = QrContentType.Url,
                overallLevel = SecurityLevel.Suspicious,
                canOpen = true,
                openableUrl = "https://example.com"
            )
        )
        val suspiciousHttpState = ResultUiState.success(
            analysis = analysis(
                contentType = QrContentType.Url,
                overallLevel = SecurityLevel.Suspicious,
                canOpen = true,
                openableUrl = "http://example.com"
            )
        )
        val dangerousState = ResultUiState.success(
            analysis = analysis(
                contentType = QrContentType.Url,
                overallLevel = SecurityLevel.Dangerous,
                canOpen = false,
                openableUrl = null
            )
        )

        assertTrue(safeHttpsState.showOpenButton)
        assertTrue(safeHttpsState.canOpen)
        assertEquals("https://example.com", safeHttpsState.openableUrl)

        assertTrue(bareDomainState.showOpenButton)
        assertTrue(bareDomainState.canOpen)
        assertEquals("https://example.com", bareDomainState.openableUrl)

        assertTrue(suspiciousHttpState.showOpenButton)
        assertTrue(suspiciousHttpState.canOpen)
        assertEquals("http://example.com", suspiciousHttpState.openableUrl)

        assertFalse(dangerousState.showOpenButton)
        assertFalse(dangerousState.canOpen)
        assertNull(dangerousState.openableUrl)
    }

    @Test
    fun `non url analyses never expose open actions`() {
        val contentTypes = listOf(
            QrContentType.PlainText,
            QrContentType.Wifi,
            QrContentType.Phone,
            QrContentType.Sms,
            QrContentType.Email,
            QrContentType.Crypto,
            QrContentType.Unknown
        )

        contentTypes.forEach { contentType ->
            val state = ResultUiState.success(
                analysis = analysis(
                    contentType = contentType,
                    overallLevel = SecurityLevel.Unknown,
                    canOpen = false,
                    openableUrl = null
                )
            )

            assertFalse(state.showOpenButton, contentType.name)
            assertFalse(state.canOpen, contentType.name)
            assertNull(state.openableUrl, contentType.name)
        }
    }

    private fun analysis(
        contentType: QrContentType,
        overallLevel: SecurityLevel,
        canOpen: Boolean,
        openableUrl: String?
    ): QrAnalysisResult {
        return QrAnalysisResult(
            originalText = "input",
            normalizedText = "input",
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
                reasons = emptyList()
            ),
            remoteReputation = ScanSectionResult(
                name = "Remote Reputation",
                level = SecurityLevel.Unknown,
                status = ScanStatus.NotApplicable,
                title = "Remote reputation not applicable",
                description = "Only URLs are checked against remote reputation providers.",
                reasons = emptyList()
            )
        )
    }
}
