package com.lmartin.qrguardian.presentation.result

import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.SecurityLevel

data class ResultUiState(
    val isLoading: Boolean = false,
    val analysis: QrAnalysisResult? = null,
    val errorMessage: String? = null,
) {
    val canOpen: Boolean
        get() = analysis?.canOpen == true

    val showOpenButton: Boolean
        get() =
            analysis?.let { analysis ->
                analysis.contentType == QrContentType.Url &&
                    analysis.canOpen &&
                    analysis.openableUrl != null
            } == true

    val openableUrl: String?
        get() = analysis?.openableUrl

    val overallLevel: SecurityLevel?
        get() = analysis?.overallLevel

    val localScan: ScanSectionResult?
        get() = analysis?.localScan

    val remoteReputation: ScanSectionResult?
        get() = analysis?.remoteReputation

    companion object {
        fun idle() = ResultUiState()

        fun loading() = ResultUiState(isLoading = true)

        fun success(analysis: QrAnalysisResult) = ResultUiState(analysis = analysis)

        fun error(message: String) = ResultUiState(errorMessage = message)
    }
}
