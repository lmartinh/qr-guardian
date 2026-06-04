package com.lmartin.qrguardian.presentation.result

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lmartin.qrguardian.domain.model.QrAnalysisResult

class ResultViewModel(
    initialState: ResultUiState = ResultUiState.idle(),
) {
    var uiState by mutableStateOf(initialState)
        private set

    fun showLoading() {
        uiState = ResultUiState.loading()
    }

    fun showResult(analysis: QrAnalysisResult) {
        uiState = ResultUiState.success(analysis)
    }

    fun showError(message: String) {
        uiState = ResultUiState.error(message)
    }

    fun reset() {
        uiState = ResultUiState.idle()
    }

    companion object {
        fun preview(state: ResultUiState): ResultViewModel = ResultViewModel(state)
    }
}
