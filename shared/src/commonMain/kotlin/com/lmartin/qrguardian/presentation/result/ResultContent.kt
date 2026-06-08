package com.lmartin.qrguardian.presentation.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.result.components.ResultActionArea
import com.lmartin.qrguardian.presentation.result.components.ResultContentCard
import com.lmartin.qrguardian.presentation.result.components.ResultHeader
import com.lmartin.qrguardian.presentation.result.components.ResultSectionCard
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
    val openButtonVisible = state.showOpenButton
    val localScanBadge = localScanBadgeLabel(analysis.localScan, texts)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        tone.accentColor.copy(alpha = 0.14f),
                    ),
                ),
            )
            .safeContentPadding()
            .verticalScroll(scrollState)
            .padding(vertical = QrGuardianSpacing.M),
        verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
    ) {
        ResultHeader(
            tone = tone,
            title = texts.levelTitle(analysis.overallLevel),
            description = texts.levelDescription(analysis.overallLevel),
            contentTypeLabel = contentTypeLabel(analysis.contentType, texts),
            statusLabel = when (analysis.overallLevel) {
                SecurityLevel.Safe -> texts.statusRecommended
                SecurityLevel.Suspicious -> texts.statusReview
                SecurityLevel.Dangerous -> texts.statusBlocked
                SecurityLevel.Unknown -> texts.statusUncertain
            },
        )

        ResultContentCard(
            title = texts.qrContentLabel,
            normalizedValue = analysis.normalizedText,
        )

        ResultSectionCard(
            title = texts.localScan,
            summary = localScanSummary(analysis.localScan, texts),
            level = analysis.localScan.level,
            levelLabel = localScanBadge,
            levelTint = tone.sectionTint(analysis.localScan.level),
            levelContentColor = tone.sectionContent(analysis.localScan.level),
            items = buildLocalAnalysisDetails(analysis, texts),
            signalsTitle = texts.localSignals,
            signals = buildLocalSignals(analysis.localScan),
            maxVisibleItems = null,
            maxVisibleSignals = 3,
        )

        ResultSectionCard(
            title = texts.remoteReputation,
            summary = texts.sectionSummary(analysis.remoteReputation),
            level = analysis.remoteReputation.level,
            levelLabel = remoteScanBadgeLabel(analysis.remoteReputation, texts),
            levelTint = tone.sectionTint(analysis.remoteReputation.level),
            levelContentColor = tone.sectionContent(analysis.remoteReputation.level),
            items = buildRemoteAnalysisDetails(analysis.remoteReputation, texts),
            maxVisibleItems = 3,
            maxVisibleSignals = 0,
        )

        ResultActionArea(
            showOpenButton = openButtonVisible,
            overallLevel = analysis.overallLevel,
            openLinkText = texts.openLink,
            rescanText = texts.rescan,
            tone = tone,
            onOpenClick = { onOpenUrl(analysis.openableUrl.orEmpty()) },
            onRescanClick = onRescanClick,
        )
    }
}
