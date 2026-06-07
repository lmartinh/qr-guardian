package com.lmartin.qrguardian.presentation.result

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lmartin.qrguardian.presentation.result.components.ResultEmptyContent
import com.lmartin.qrguardian.presentation.result.components.ResultErrorContent
import com.lmartin.qrguardian.presentation.result.components.ResultLoadingContent

@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onOpenUrl: (String) -> Unit,
    onRescanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.uiState
    val texts = rememberResultTexts()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when {
            state.isLoading -> ResultLoadingContent(texts)
            state.errorMessage != null -> ResultErrorContent(
                message = state.errorMessage,
                onRescanClick = onRescanClick,
                texts = texts,
            )
            state.analysis != null -> ResultContent(
                state = state,
                onOpenUrl = onOpenUrl,
                onRescanClick = onRescanClick,
                texts = texts,
            )
            else -> ResultEmptyContent(
                onRescanClick = onRescanClick,
                texts = texts,
            )
        }
    }
}
