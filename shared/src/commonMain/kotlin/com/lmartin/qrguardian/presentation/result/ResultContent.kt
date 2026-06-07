package com.lmartin.qrguardian.presentation.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.result.components.ResultContentCard
import com.lmartin.qrguardian.presentation.result.components.ResultHeader
import com.lmartin.qrguardian.presentation.result.components.ResultOpenLinkButton
import com.lmartin.qrguardian.presentation.result.components.ResultRescanButton
import com.lmartin.qrguardian.presentation.result.components.ResultSectionCard
import com.lmartin.qrguardian.presentation.result.components.ResultTechnicalCard
import com.lmartin.qrguardian.presentation.theme.QrGuardianSpacing

@Composable
internal fun ResultContent(
    state: ResultUiState,
    onOpenUrl: (String) -> Unit,
    onRescanClick: () -> Unit,
    texts: ResultTexts,
) {
    val analysis = state.analysis ?: return
    val tone = analysis.overallLevel.toResultTone()
    val scrollState = rememberScrollState()
    val openButtonVisible = state.showOpenButton && state.openableUrl != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        tone.accentColor.copy(alpha = 0.14f),
                    )
                )
            )
            .safeContentPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = QrGuardianSpacing.M, vertical = QrGuardianSpacing.M),
        verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
    ) {
        ResultHeader(
            tone = tone,
            title = analysis.overallLevel.title(),
            description = analysis.overallLevel.description(),
            contentTypeLabel = contentTypeLabel(analysis.contentType, texts),
            statusLabel = when (analysis.overallLevel) {
                SecurityLevel.Safe -> texts.statusRecommended
                SecurityLevel.Suspicious -> texts.statusReview
                SecurityLevel.Dangerous -> texts.statusBlocked
                SecurityLevel.Unknown -> texts.statusUncertain
            },
        )

        if (openButtonVisible) {
            ResultOpenLinkButton(
                text = texts.openLink,
                tone = tone,
                onClick = { onOpenUrl(analysis.openableUrl.orEmpty()) },
            )
        }

        ResultContentCard(
            title = texts.detailType,
            icon = tone.icon,
            accent = contentAccentColor(analysis.contentType),
            items = buildContentDetails(analysis, texts),
            normalizedValue = analysis.normalizedText,
        )

        ResultSectionCard(
            title = texts.localScan,
            summary = analysis.localScan.title,
            levelLabel = analysis.localScan.level.title(),
            levelTint = tone.sectionTint(analysis.localScan.level),
            levelContentColor = tone.sectionContent(analysis.localScan.level),
            items = buildLocalAnalysisDetails(analysis, texts),
        )

        ResultSectionCard(
            title = texts.remoteReputation,
            summary = analysis.remoteReputation.title,
            levelLabel = analysis.remoteReputation.level.title(),
            levelTint = tone.sectionTint(analysis.remoteReputation.level),
            levelContentColor = tone.sectionContent(analysis.remoteReputation.level),
            items = buildRemoteAnalysisDetails(analysis.remoteReputation, texts),
        )

        ResultTechnicalCard(
            title = texts.detailState,
            items = buildTechnicalDetails(analysis, texts),
        )

        ResultRescanButton(
            text = texts.rescan,
            onClick = onRescanClick,
        )
    }
}
