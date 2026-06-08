package com.lmartin.qrguardian.presentation.result

import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultUiStateTest {
    @Test
    fun `url analyses gate the open button and expose openable urls`() {
        val safeHttpsState =
            ResultUiState.success(
                analysis =
                analysis(
                    contentType = QrContentType.Url,
                    overallLevel = SecurityLevel.Safe,
                    canOpen = true,
                    openableUrl = "https://example.com",
                ),
            )
        val bareDomainState =
            ResultUiState.success(
                analysis =
                analysis(
                    contentType = QrContentType.Url,
                    overallLevel = SecurityLevel.Suspicious,
                    canOpen = true,
                    openableUrl = "https://example.com",
                ),
            )
        val suspiciousHttpState =
            ResultUiState.success(
                analysis =
                analysis(
                    contentType = QrContentType.Url,
                    overallLevel = SecurityLevel.Suspicious,
                    canOpen = true,
                    openableUrl = "http://example.com",
                ),
            )
        val dangerousState =
            ResultUiState.success(
                analysis =
                analysis(
                    contentType = QrContentType.Url,
                    overallLevel = SecurityLevel.Dangerous,
                    canOpen = false,
                    openableUrl = null,
                ),
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
        val contentTypes =
            listOf(
                QrContentType.PlainText,
                QrContentType.Wifi,
                QrContentType.Phone,
                QrContentType.Sms,
                QrContentType.Email,
                QrContentType.Crypto,
                QrContentType.Unknown,
            )

        contentTypes.forEach { contentType ->
            val state =
                ResultUiState.success(
                    analysis =
                    analysis(
                        contentType = contentType,
                        overallLevel = SecurityLevel.Unknown,
                        canOpen = false,
                        openableUrl = null,
                    ),
                )

            assertFalse(state.showOpenButton, contentType.name)
            assertFalse(state.canOpen, contentType.name)
            assertNull(state.openableUrl, contentType.name)
        }
    }

    @Test
    fun `remote reputation and local scan sections are surfaced in ui state`() {
        val configuredRemoteState =
            ResultUiState.success(
                analysis =
                analysis(
                    contentType = QrContentType.Url,
                    overallLevel = SecurityLevel.Suspicious,
                    canOpen = true,
                    openableUrl = "https://example.com",
                    remoteStatus = ScanStatus.NotConfigured,
                ),
            )
        val notApplicableRemoteState =
            ResultUiState.success(
                analysis =
                analysis(
                    contentType = QrContentType.PlainText,
                    overallLevel = SecurityLevel.Unknown,
                    canOpen = false,
                    openableUrl = null,
                    remoteStatus = ScanStatus.NotApplicable,
                ),
            )

        assertEquals(ScanStatus.NotConfigured, configuredRemoteState.remoteReputation?.status)
        assertEquals(ScanStatus.NotApplicable, notApplicableRemoteState.remoteReputation?.status)
        assertEquals(SecurityLevel.Suspicious, configuredRemoteState.localScan?.level)
        assertEquals(SecurityLevel.Unknown, notApplicableRemoteState.localScan?.level)
    }

    private fun analysis(
        contentType: QrContentType,
        overallLevel: SecurityLevel,
        canOpen: Boolean,
        openableUrl: String?,
        remoteStatus: ScanStatus = ScanStatus.NotApplicable,
    ): QrAnalysisResult = QrAnalysisResult(
        originalText = "input",
        normalizedText = "input",
        openableUrl = openableUrl,
        contentType = contentType,
        overallLevel = overallLevel,
        canOpen = canOpen,
        localScan =
        ScanSectionResult(
            name = "Local Scan",
            level = overallLevel,
            status = ScanStatus.Completed,
            title = overallLevel.title(),
            description = overallLevel.description(),
            reasons = emptyList(),
        ),
        remoteReputation =
        ScanSectionResult(
            name = "Remote Reputation",
            level = SecurityLevel.Unknown,
            status = remoteStatus,
            title =
            when (remoteStatus) {
                ScanStatus.NotConfigured -> "Remote reputation not configured"
                ScanStatus.NotApplicable -> "Remote reputation not applicable"
                ScanStatus.Completed -> SecurityLevel.Unknown.title()
                ScanStatus.Unavailable -> "Remote reputation unavailable"
            },
            description =
            when (remoteStatus) {
                ScanStatus.NotConfigured -> {
                    "No remote reputation providers are configured for this QR Guardian instance."
                }

                ScanStatus.NotApplicable -> {
                    "Only URLs are checked against remote reputation providers."
                }

                ScanStatus.Completed -> {
                    "The remote reputation provider did not report threats for this destination."
                }

                ScanStatus.Unavailable -> {
                    "The remote reputation provider could not be reached."
                }
            },
            reasons = emptyList(),
        ),
    )
}
