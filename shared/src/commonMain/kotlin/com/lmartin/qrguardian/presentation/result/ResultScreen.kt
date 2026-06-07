package com.lmartin.qrguardian.presentation.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lmartin.qrguardian.domain.model.QrAnalysisResult
import com.lmartin.qrguardian.domain.model.QrContentType
import com.lmartin.qrguardian.domain.model.ScanMetadataItem
import com.lmartin.qrguardian.domain.model.ScanSectionResult
import com.lmartin.qrguardian.domain.model.ScanStatus
import com.lmartin.qrguardian.domain.model.SecurityLevel
import com.lmartin.qrguardian.presentation.theme.QrGuardianColors
import com.lmartin.qrguardian.presentation.theme.QrGuardianSpacing

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
            else -> ResultEmptyContent(onRescanClick = onRescanClick, texts = texts)
        }
    }
}

@Composable
private fun ResultContent(
    state: ResultUiState,
    onOpenUrl: (String) -> Unit,
    onRescanClick: () -> Unit,
    texts: ResultTexts,
) {
    val analysis = state.analysis ?: return
    val level = analysis.overallLevel
    val tone = level.toResultTone()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        tone.glowColor.copy(alpha = 0.18f),
                    )
                )
            )
            .safeContentPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = QrGuardianSpacing.M, vertical = QrGuardianSpacing.M),
        verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        ) {
            Text(
                text = texts.title,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ResultHeader(
            level = level,
            title = level.title(),
            description = level.description(),
            tone = tone,
            texts = texts,
        )

        state.openableUrl?.let { openableUrl ->
            if (state.showOpenButton) {
                PrimaryActionButton(
                    text = texts.openLink,
                    tone = tone,
                    onClick = { onOpenUrl(openableUrl) },
                )
            }
        }

        ResultUrlCard(
            url = analysis.normalizedText,
            texts = texts,
        )

        SectionCard(
            title = texts.localScan,
            section = analysis.localScan,
            tone = tone,
            detailItems = buildLocalDetails(analysis, texts),
            reasonGroupLabel = texts.localSignals,
            texts = texts,
        )

        SectionCard(
            title = texts.remoteReputation,
            section = analysis.remoteReputation,
            tone = tone,
            detailItems = buildRemoteDetails(analysis.remoteReputation, texts),
            reasonGroupLabel = texts.remoteChecks,
            texts = texts,
        )

        Spacer(modifier = Modifier.height(QrGuardianSpacing.Xs))

        OutlinedButton(
            onClick = onRescanClick,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                text = texts.rescan,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ResultHeader(
    level: SecurityLevel,
    title: String,
    description: String,
    tone: ResultTone,
    texts: ResultTexts,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(
                    color = tone.glowColor.copy(alpha = 0.16f),
                    shape = MaterialTheme.shapes.extraLarge,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(
                        color = tone.badgeColor,
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tone.icon,
                    contentDescription = null,
                    tint = tone.badgeContentColor,
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill(
                label = level.name.lowercase().replaceFirstChar { it.uppercase() },
                backgroundColor = tone.pillColor,
                contentColor = tone.pillContentColor,
            )
            StatusPill(
                label = when (level) {
                    SecurityLevel.Safe -> texts.statusRecommended
                    SecurityLevel.Suspicious -> texts.statusReview
                    SecurityLevel.Dangerous -> texts.statusBlocked
                    SecurityLevel.Unknown -> texts.statusUncertain
                },
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResultUrlCard(
    url: String,
    texts: ResultTexts,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QrGuardianSpacing.M),
            verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
        ) {
            Text(
                text = texts.urlLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    section: ScanSectionResult,
    tone: ResultTone,
    detailItems: List<ResultDetailItem>,
    reasonGroupLabel: String,
    texts: ResultTexts,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(QrGuardianSpacing.M),
                verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.M),
            ) {
                if (section.status == ScanStatus.NotConfigured) {
                    RemoteNotConfiguredCard(
                        title = title,
                        tone = tone,
                        texts = texts,
                    )
                    return@Column
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusPill(
                    label = section.level.title(),
                    backgroundColor = tone.sectionTint(section.level),
                    contentColor = tone.sectionContent(section.level),
                )
            }

            Text(
                text = section.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (detailItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S)) {
                    detailItems.forEach { item ->
                        DetailRow(item = item)
                    }
                }
            }

            if (section.reasons.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S)) {
                    Text(
                        text = reasonGroupLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    section.reasons.take(3).forEach { reason ->
                        ReasonRow(text = reason)
                    }
                }
            } else if (
                section.status == ScanStatus.NotConfigured ||
                section.status == ScanStatus.NotApplicable ||
                section.status == ScanStatus.Unavailable
            ) {
                EmptyCheckState(
                    text = when (section.status) {
                        ScanStatus.NotConfigured -> texts.remoteNotConfigured
                        ScanStatus.NotApplicable -> texts.remoteNotApplicable
                        ScanStatus.Unavailable -> texts.remoteUnavailable
                        ScanStatus.Completed -> texts.remoteCompleted
                    }
                )
            }

            if (section.metadata.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                Column(verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S)) {
                    section.metadata.take(3).forEach { item ->
                        MetadataRow(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteNotConfiguredCard(
    title: String,
    tone: ResultTone,
    texts: ResultTexts,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            StatusPill(
                label = texts.remoteNotConfiguredStatus,
                backgroundColor = tone.sectionTint(SecurityLevel.Unknown),
                contentColor = tone.sectionContent(SecurityLevel.Unknown),
            )
        }

        Text(
            text = texts.remoteNotConfigured,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

    }
}

@Composable
private fun ResultErrorContent(
    message: String,
    onRescanClick: () -> Unit,
    texts: ResultTexts,
) {
    PlaceholderContent(
        title = texts.errorTitle,
        message = message,
        actionLabel = texts.rescan,
        onActionClick = onRescanClick,
    )
}

@Composable
private fun ResultEmptyContent(
    onRescanClick: () -> Unit,
    texts: ResultTexts,
) {
    PlaceholderContent(
        title = texts.idleTitle,
        message = texts.idleMessage,
        actionLabel = texts.rescan,
        onActionClick = onRescanClick,
    )
}

@Composable
private fun ResultLoadingContent(texts: ResultTexts) {
    PlaceholderContent(
        title = texts.loadingTitle,
        message = texts.loadingMessage,
        actionLabel = texts.loadingAction,
        onActionClick = {},
        actionEnabled = false,
    )
}

@Composable
private fun PlaceholderContent(
    title: String,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    actionEnabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = QrGuardianSpacing.L, vertical = QrGuardianSpacing.Xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
        ) {
            Box(
                modifier = Modifier.size(88.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(QrGuardianSpacing.L))
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(QrGuardianSpacing.S))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(QrGuardianSpacing.L))
        Button(
            onClick = onActionClick,
            enabled = actionEnabled,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(text = actionLabel)
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    tone: ResultTone,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = tone.buttonContainerColor,
            contentColor = tone.buttonContentColor,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DetailRow(item: ResultDetailItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
        verticalAlignment = Alignment.Top,
    ) {
        when {
            item.leadingIcon != null -> Icon(
                imageVector = item.leadingIcon,
                contentDescription = null,
                tint = item.color,
                modifier = Modifier.size(18.dp),
            )
            item.leadingText != null -> Text(
                text = item.leadingText,
                style = MaterialTheme.typography.labelLarge,
                color = item.color,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = item.value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun EmptyCheckState(text: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReasonRow(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MetadataRow(item: ScanMetadataItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = item.value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatusPill(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = backgroundColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
    }
}

private data class ResultTone(
    val icon: ImageVector,
    val badgeColor: Color,
    val badgeContentColor: Color,
    val glowColor: Color,
    val pillColor: Color,
    val pillContentColor: Color,
    val buttonContainerColor: Color,
    val buttonContentColor: Color,
) {
    fun sectionTint(level: SecurityLevel): Color = when (level) {
        SecurityLevel.Safe -> QrGuardianColors.SafeContainerLight
        SecurityLevel.Suspicious -> QrGuardianColors.WarningContainerLight
        SecurityLevel.Dangerous -> QrGuardianColors.DangerContainerLight
        SecurityLevel.Unknown -> QrGuardianColors.Secondary.copy(alpha = 0.16f)
    }

    fun sectionContent(level: SecurityLevel): Color = when (level) {
        SecurityLevel.Safe -> QrGuardianColors.Safe
        SecurityLevel.Suspicious -> Color(0xFF9A6B00)
        SecurityLevel.Dangerous -> Color(0xFFB42318)
        SecurityLevel.Unknown -> QrGuardianColors.PrimaryDark
    }
}

private data class ResultDetailItem(
    val leadingText: String?,
    val leadingIcon: ImageVector?,
    val label: String,
    val value: String,
    val color: Color,
)

private fun SecurityLevel.toResultTone(): ResultTone {
    return when (this) {
        SecurityLevel.Safe -> ResultTone(
            icon = Icons.Filled.CheckCircle,
            badgeColor = QrGuardianColors.SafeContainerLight,
            badgeContentColor = QrGuardianColors.Safe,
            glowColor = QrGuardianColors.Safe,
            pillColor = QrGuardianColors.SafeContainerLight,
            pillContentColor = QrGuardianColors.Safe,
            buttonContainerColor = QrGuardianColors.PrimaryDark,
            buttonContentColor = Color.White,
        )
        SecurityLevel.Suspicious -> ResultTone(
            icon = Icons.Filled.Warning,
            badgeColor = QrGuardianColors.WarningContainerLight,
            badgeContentColor = Color(0xFF9A6B00),
            glowColor = QrGuardianColors.Warning,
            pillColor = QrGuardianColors.WarningContainerLight,
            pillContentColor = Color(0xFF9A6B00),
            buttonContainerColor = QrGuardianColors.PrimaryDark.copy(alpha = 0.88f),
            buttonContentColor = Color.White,
        )
        SecurityLevel.Dangerous -> ResultTone(
            icon = Icons.Filled.Error,
            badgeColor = QrGuardianColors.DangerContainerLight,
            badgeContentColor = Color(0xFFB42318),
            glowColor = QrGuardianColors.Danger,
            pillColor = QrGuardianColors.DangerContainerLight,
            pillContentColor = Color(0xFFB42318),
            buttonContainerColor = QrGuardianColors.PrimaryDark.copy(alpha = 0.76f),
            buttonContentColor = Color.White,
        )
        SecurityLevel.Unknown -> ResultTone(
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            badgeColor = QrGuardianColors.Secondary.copy(alpha = 0.24f),
            badgeContentColor = QrGuardianColors.PrimaryDark,
            glowColor = QrGuardianColors.Neutral,
            pillColor = QrGuardianColors.Secondary.copy(alpha = 0.18f),
            pillContentColor = QrGuardianColors.PrimaryDark,
            buttonContainerColor = QrGuardianColors.PrimaryDark.copy(alpha = 0.84f),
            buttonContentColor = Color.White,
        )
    }
}

private fun buildLocalDetails(
    analysis: QrAnalysisResult,
    texts: ResultTexts,
): List<ResultDetailItem> {
    return when (analysis.contentType) {
        QrContentType.Url -> buildUrlLocalDetails(analysis.normalizedText, texts)
        QrContentType.Email -> listOf(
            detailItem("✉", texts.detailType, texts.detailContact, QrGuardianColors.PrimaryDark),
            detailItem("@", texts.detailAction, texts.emailAction, QrGuardianColors.PrimaryDark),
            detailItem("→", texts.detailDestination, extractMailTarget(analysis.normalizedText, texts), QrGuardianColors.PrimaryDark),
            detailItem("↧", texts.detailExtraField, extractMailExtras(analysis.normalizedText, texts), QrGuardianColors.PrimaryDark),
        )
        QrContentType.Phone -> listOf(
            detailItem("☎", texts.detailType, texts.phoneType, QrGuardianColors.PrimaryDark),
            detailItem("↗", texts.detailAction, texts.phoneAction, QrGuardianColors.PrimaryDark),
            detailItem("#", texts.detailNumber, extractTelephoneTarget(analysis.normalizedText, texts), QrGuardianColors.PrimaryDark),
        )
        QrContentType.Sms -> listOf(
            detailItem("✉", texts.detailType, texts.smsType, QrGuardianColors.PrimaryDark),
            detailItem("↗", texts.detailAction, texts.smsAction, QrGuardianColors.PrimaryDark),
            detailItem("#", texts.detailDestination, extractSmsTarget(analysis.normalizedText, texts), QrGuardianColors.PrimaryDark),
            detailItem("⌕", texts.detailMessage, extractSmsBody(analysis.normalizedText, texts), QrGuardianColors.PrimaryDark),
        )
        QrContentType.Wifi -> listOf(
            detailItem("≋", texts.detailType, texts.wifiType, QrGuardianColors.PrimaryDark),
            detailItem("⌁", texts.detailAction, texts.wifiAction, QrGuardianColors.PrimaryDark),
            detailItem(texts.detailSsidKey, texts.detailNetwork, extractWifiField(analysis.normalizedText, "S", texts), QrGuardianColors.PrimaryDark),
            detailItem(texts.detailSecurityKey, texts.detailSecurity, extractWifiField(analysis.normalizedText, "T", texts), QrGuardianColors.PrimaryDark),
        )
        QrContentType.VCard -> listOf(
            detailItem("👤", texts.detailType, texts.vCardType, QrGuardianColors.PrimaryDark),
            detailItem("↗", texts.detailAction, texts.importContactAction, QrGuardianColors.PrimaryDark),
        )
        QrContentType.Geo -> listOf(
            detailItem("⌖", texts.detailType, texts.locationType, QrGuardianColors.PrimaryDark),
            detailItem("↗", texts.detailAction, texts.openMapAction, QrGuardianColors.PrimaryDark),
        )
        QrContentType.Crypto -> listOf(
            detailItem("◈", texts.detailType, texts.cryptoType, QrGuardianColors.PrimaryDark),
            detailItem("↗", texts.detailAction, texts.cryptoAction, QrGuardianColors.PrimaryDark),
        )
        QrContentType.PlainText -> listOf(
            detailItem("⇲", texts.detailType, texts.plainTextType, QrGuardianColors.PrimaryDark),
            detailItem("i", texts.detailUsage, texts.notALink, QrGuardianColors.PrimaryDark),
        )
        QrContentType.Unknown -> listOf(
            detailItem("?", texts.detailType, texts.unknownType, QrGuardianColors.PrimaryDark),
            detailItem("i", texts.detailState, texts.notClassifiedPrecisely, QrGuardianColors.PrimaryDark),
        )
    }
}

private fun buildUrlLocalDetails(url: String, texts: ResultTexts): List<ResultDetailItem> {
    val details = mutableListOf<ResultDetailItem>()
    val downloadType = detectDownloadType(url, texts)
    val path = extractUrlPath(url)

    if (downloadType.isNotBlank()) {
        details += detailItem(
            leadingIcon = Icons.Outlined.Description,
            label = texts.detailFile,
            value = downloadType,
            color = QrGuardianColors.PrimaryDark,
        )
    }
    if (path.isNotBlank()) {
        details += detailItem(
            leadingIcon = Icons.Outlined.SubdirectoryArrowRight,
            label = texts.detailPath,
            value = path,
            color = QrGuardianColors.PrimaryDark,
        )
    }

    return details
}

private fun extractUrlPath(url: String): String {
    val withoutScheme = url.substringAfter("://", url)
    val pathStart = withoutScheme.indexOf('/')
    if (pathStart < 0) return ""
    val pathWithQuery = withoutScheme.substring(pathStart)
    return pathWithQuery.substringBefore('?')
}

private fun detectDownloadType(url: String, texts: ResultTexts): String {
    val path = extractUrlPath(url).lowercase()
    return when {
        path.endsWith(".apk") -> texts.fileTypeApk
        path.endsWith(".pdf") -> texts.fileTypePdf
        path.endsWith(".zip") -> texts.fileTypeZip
        path.endsWith(".rar") -> texts.fileTypeRar
        path.endsWith(".7z") -> texts.fileTypeSevenZip
        path.endsWith(".doc") || path.endsWith(".docx") -> texts.fileTypeDocument
        path.endsWith(".xls") || path.endsWith(".xlsx") -> texts.fileTypeSpreadsheet
        path.endsWith(".ppt") || path.endsWith(".pptx") -> texts.fileTypePresentation
        path.endsWith(".mp3") || path.endsWith(".wav") -> texts.fileTypeAudio
        path.endsWith(".mp4") || path.endsWith(".mov") || path.endsWith(".mkv") -> texts.fileTypeVideo
        path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".webp") -> texts.fileTypeImage
        else -> ""
    }
}

private fun buildRemoteDetails(
    section: ScanSectionResult,
    texts: ResultTexts,
): List<ResultDetailItem> {
    val details = mutableListOf<ResultDetailItem>()

    details += detailItem(
        leading = when (section.status) {
            ScanStatus.Completed -> "✓"
            ScanStatus.NotConfigured -> "!"
            ScanStatus.NotApplicable -> "—"
            ScanStatus.Unavailable -> "?"
        },
        label = texts.detailState,
        value = when (section.status) {
            ScanStatus.Completed -> texts.remoteCompleted
            ScanStatus.NotConfigured -> texts.remoteNotConfiguredStatus
            ScanStatus.NotApplicable -> texts.remoteNotApplicableStatus
            ScanStatus.Unavailable -> texts.remoteUnavailableStatus
        },
        color = when (section.level) {
            SecurityLevel.Safe -> QrGuardianColors.Safe
            SecurityLevel.Suspicious -> Color(0xFF9A6B00)
            SecurityLevel.Dangerous -> Color(0xFFB42318)
            SecurityLevel.Unknown -> QrGuardianColors.PrimaryDark
        },
    )

    if (section.metadata.isNotEmpty()) {
        section.metadata.take(3).forEach { item ->
            details += detailItem("•", item.label, item.value, QrGuardianColors.PrimaryDark)
        }
    }

    return details
}

private fun detailItem(
    leading: String,
    label: String,
    value: String,
    color: Color,
): ResultDetailItem {
    return ResultDetailItem(
        leadingText = leading,
        leadingIcon = null,
        label = label,
        value = value,
        color = color,
    )
}

private fun detailItem(
    leadingIcon: ImageVector,
    label: String,
    value: String,
    color: Color,
): ResultDetailItem {
    return ResultDetailItem(
        leadingText = null,
        leadingIcon = leadingIcon,
        label = label,
        value = value,
        color = color,
    )
}

private fun extractMailTarget(text: String, texts: ResultTexts): String {
    return text.removePrefix("mailto:").substringBefore('?').ifBlank { texts.noValue }
}

private fun extractMailExtras(text: String, texts: ResultTexts): String {
    val query = text.substringAfter('?', "")
    return when {
        query.isBlank() -> texts.noSubjectOrBody
        query.contains("subject=") || query.contains("body=") -> texts.predefinedFields
        else -> texts.parametersPresent
    }
}

private fun extractTelephoneTarget(text: String, texts: ResultTexts): String {
    return text.removePrefix("tel:").ifBlank { texts.noValue }
}

private fun extractSmsTarget(text: String, texts: ResultTexts): String {
    return text.removePrefix("sms:").substringBefore('?').ifBlank { texts.noValue }
}

private fun extractSmsBody(text: String, texts: ResultTexts): String {
    val body = text.substringAfter("body=", "")
    return if (body.isBlank()) texts.noPredefinedMessage else body.substringBefore('&')
}

private fun extractWifiField(text: String, key: String, texts: ResultTexts): String {
    val payload = text.removePrefix("WIFI:")
    return payload.split(';')
        .asSequence()
        .mapNotNull { entry ->
            val separatorIndex = entry.indexOf(':')
            if (separatorIndex <= 0) return@mapNotNull null
            val entryKey = entry.substring(0, separatorIndex).uppercase()
            val entryValue = entry.substring(separatorIndex + 1)
            if (entryKey == key) entryValue else null
        }
        .firstOrNull()
        .orEmpty()
        .ifBlank { texts.noValue }
}
