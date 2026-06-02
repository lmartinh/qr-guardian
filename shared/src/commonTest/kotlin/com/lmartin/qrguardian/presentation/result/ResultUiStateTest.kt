package com.lmartin.qrguardian.presentation.result

import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResultUiStateTest {
    @Test
    fun `non url analysis hides open button`() {
        val state = ResultUiState.success(
            analysis = analysis(
                contentType = QrContentType.PlainText,
                canOpen = false
            )
        )

        assertFalse(state.showOpenButton)
    }

    @Test
    fun `url analysis shows open button when safe`() {
        val state = ResultUiState.success(
            analysis = analysis(
                contentType = QrContentType.Url,
                canOpen = true
            )
        )

        assertTrue(state.showOpenButton)
    }

    private fun analysis(
        contentType: QrContentType,
        canOpen: Boolean
    ): QrAnalysisResult {
        return QrAnalysisResult(
            originalText = "input",
            normalizedText = "input",
            contentType = contentType,
            overallLevel = SecurityLevel.Safe,
            canOpen = canOpen,
            localScan = ScanSectionResult(
                name = "Local Scan",
                level = SecurityLevel.Safe,
                status = ScanStatus.Completed,
                title = SecurityLevel.Safe.title(),
                description = SecurityLevel.Safe.description(),
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
