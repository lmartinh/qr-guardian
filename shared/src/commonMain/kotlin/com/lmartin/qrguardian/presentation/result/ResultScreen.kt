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
import org.jetbrains.compose.resources.stringResource
import qrguardian.shared.generated.resources.Res
import qrguardian.shared.generated.resources.result_local_scan
import qrguardian.shared.generated.resources.result_open_link
import qrguardian.shared.generated.resources.result_remote_reputation
import qrguardian.shared.generated.resources.result_rescan
import qrguardian.shared.generated.resources.result_title
import qrguardian.shared.generated.resources.result_url_analyzed

@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onOpenLinkClick: (String) -> Unit,
    onRescanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.uiState

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when {
            state.isLoading -> ResultLoadingContent()
            state.errorMessage != null -> ResultErrorContent(
                message = state.errorMessage,
                onRescanClick = onRescanClick,
            )
            state.analysis != null -> ResultContent(
                state = state,
                onOpenLinkClick = onOpenLinkClick,
                onRescanClick = onRescanClick,
            )
            else -> ResultEmptyContent(onRescanClick = onRescanClick)
        }
    }
}

@Composable
private fun ResultContent(
    state: ResultUiState,
    onOpenLinkClick: (String) -> Unit,
    onRescanClick: () -> Unit,
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
                text = stringResource(Res.string.result_title),
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
        )

        PrimaryActionButton(
            text = stringResource(Res.string.result_open_link),
            tone = tone,
            onClick = { onOpenLinkClick(analysis.normalizedText) },
        )

        ResultUrlCard(
            url = analysis.normalizedText,
            detailItems = emptyList(),
        )

        SectionCard(
            title = stringResource(Res.string.result_local_scan),
            section = analysis.localScan,
            tone = tone,
            detailItems = buildLocalDetails(analysis),
            reasonGroupLabel = "Señales locales",
        )

        SectionCard(
            title = stringResource(Res.string.result_remote_reputation),
            section = analysis.remoteReputation,
            tone = tone,
            detailItems = buildRemoteDetails(analysis.remoteReputation),
            reasonGroupLabel = "Verificaciones remotas",
        )

        Spacer(modifier = Modifier.height(QrGuardianSpacing.Xs))

        OutlinedButton(
            onClick = onRescanClick,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                text = stringResource(Res.string.result_rescan),
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
                Text(
                    text = tone.symbol,
                    style = MaterialTheme.typography.displayMedium,
                    color = tone.badgeContentColor,
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
                    SecurityLevel.Safe -> "Recomendado"
                    SecurityLevel.Suspicious -> "Revisar"
                    SecurityLevel.Dangerous -> "Bloqueado"
                    SecurityLevel.Unknown -> "Sin certeza"
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
    detailItems: List<ResultDetailItem>,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.result_url_analyzed),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (detailItems.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                Column(verticalArrangement = Arrangement.spacedBy(QrGuardianSpacing.S)) {
                    detailItems.forEach { item ->
                        DetailRow(item = item)
                    }
                }
            }
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
                        ScanStatus.NotConfigured -> "La reputación remota no está configurada."
                        ScanStatus.NotApplicable -> "No aplica para este tipo de contenido."
                        ScanStatus.Unavailable -> "No hay verificaciones remotas disponibles en este momento."
                        ScanStatus.Completed -> ""
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
private fun ResultErrorContent(
    message: String,
    onRescanClick: () -> Unit,
) {
    PlaceholderContent(
        title = "No pudimos mostrar el resultado",
        message = message,
        actionLabel = stringResource(Res.string.result_rescan),
        onActionClick = onRescanClick,
    )
}

@Composable
private fun ResultEmptyContent(
    onRescanClick: () -> Unit,
) {
    PlaceholderContent(
        title = "Resultado pendiente",
        message = "Cuando haya un análisis disponible, se mostrará aquí.",
        actionLabel = stringResource(Res.string.result_rescan),
        onActionClick = onRescanClick,
    )
}

@Composable
private fun ResultLoadingContent() {
    PlaceholderContent(
        title = "Analizando...",
        message = "Estamos preparando el diagnóstico del enlace.",
        actionLabel = "Espera un momento",
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
                Text(
                    text = "↻",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
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
        Text(
            text = item.leading,
            style = MaterialTheme.typography.labelLarge,
            color = item.color,
        )
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
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
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
    val symbol: String,
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
    val leading: String,
    val label: String,
    val value: String,
    val color: Color,
)

private fun SecurityLevel.toResultTone(): ResultTone {
    return when (this) {
        SecurityLevel.Safe -> ResultTone(
            symbol = "✓",
            badgeColor = QrGuardianColors.SafeContainerLight,
            badgeContentColor = QrGuardianColors.Safe,
            glowColor = QrGuardianColors.Safe,
            pillColor = QrGuardianColors.SafeContainerLight,
            pillContentColor = QrGuardianColors.Safe,
            buttonContainerColor = QrGuardianColors.PrimaryDark,
            buttonContentColor = Color.White,
        )
        SecurityLevel.Suspicious -> ResultTone(
            symbol = "!",
            badgeColor = QrGuardianColors.WarningContainerLight,
            badgeContentColor = Color(0xFF9A6B00),
            glowColor = QrGuardianColors.Warning,
            pillColor = QrGuardianColors.WarningContainerLight,
            pillContentColor = Color(0xFF9A6B00),
            buttonContainerColor = QrGuardianColors.PrimaryDark.copy(alpha = 0.88f),
            buttonContentColor = Color.White,
        )
        SecurityLevel.Dangerous -> ResultTone(
            symbol = "!",
            badgeColor = QrGuardianColors.DangerContainerLight,
            badgeContentColor = Color(0xFFB42318),
            glowColor = QrGuardianColors.Danger,
            pillColor = QrGuardianColors.DangerContainerLight,
            pillContentColor = Color(0xFFB42318),
            buttonContainerColor = QrGuardianColors.PrimaryDark.copy(alpha = 0.76f),
            buttonContentColor = Color.White,
        )
        SecurityLevel.Unknown -> ResultTone(
            symbol = "?",
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

private fun buildLocalDetails(analysis: QrAnalysisResult): List<ResultDetailItem> {
    return when (analysis.contentType) {
        QrContentType.Url -> buildUrlLocalDetails(analysis.normalizedText)
        QrContentType.Email -> listOf(
            detailItem("✉", "Tipo", "Correo electrónico", QrGuardianColors.PrimaryDark),
            detailItem("@", "Acción", "Abrir cliente de correo", QrGuardianColors.PrimaryDark),
            detailItem("→", "Destino", extractMailTarget(analysis.normalizedText), QrGuardianColors.PrimaryDark),
            detailItem("↧", "Campo extra", extractMailExtras(analysis.normalizedText), QrGuardianColors.PrimaryDark),
        )
        QrContentType.Phone -> listOf(
            detailItem("☎", "Tipo", "Número de teléfono", QrGuardianColors.PrimaryDark),
            detailItem("↗", "Acción", "Iniciar llamada", QrGuardianColors.PrimaryDark),
            detailItem("#", "Número", extractTelephoneTarget(analysis.normalizedText), QrGuardianColors.PrimaryDark),
        )
        QrContentType.Sms -> listOf(
            detailItem("✉", "Tipo", "SMS", QrGuardianColors.PrimaryDark),
            detailItem("↗", "Acción", "Abrir app de mensajería", QrGuardianColors.PrimaryDark),
            detailItem("#", "Destino", extractSmsTarget(analysis.normalizedText), QrGuardianColors.PrimaryDark),
            detailItem("⌕", "Mensaje", extractSmsBody(analysis.normalizedText), QrGuardianColors.PrimaryDark),
        )
        QrContentType.Wifi -> listOf(
            detailItem("≋", "Tipo", "Wi‑Fi", QrGuardianColors.PrimaryDark),
            detailItem("⌁", "Acción", "Configurar red", QrGuardianColors.PrimaryDark),
            detailItem("SSID", "Red", extractWifiField(analysis.normalizedText, "S"), QrGuardianColors.PrimaryDark),
            detailItem("T", "Seguridad", extractWifiField(analysis.normalizedText, "T"), QrGuardianColors.PrimaryDark),
        )
        QrContentType.VCard -> listOf(
            detailItem("👤", "Tipo", "Contacto / vCard", QrGuardianColors.PrimaryDark),
        detailItem("↗", "Acción", "Importar contacto", QrGuardianColors.PrimaryDark),
        )
        QrContentType.Geo -> listOf(
            detailItem("⌖", "Tipo", "Ubicación", QrGuardianColors.PrimaryDark),
        detailItem("↗", "Acción", "Abrir mapa", QrGuardianColors.PrimaryDark),
        )
        QrContentType.Crypto -> listOf(
            detailItem("◈", "Tipo", "Pago cripto", QrGuardianColors.PrimaryDark),
        detailItem("↗", "Acción", "Iniciar transferencia", QrGuardianColors.PrimaryDark),
        )
        QrContentType.PlainText -> listOf(
            detailItem("⇲", "Tipo", "Texto plano", QrGuardianColors.PrimaryDark),
            detailItem("i", "Uso", "No es un enlace ni una acción directa", QrGuardianColors.PrimaryDark),
        )
        QrContentType.Unknown -> listOf(
            detailItem("?", "Tipo", "Desconocido", QrGuardianColors.PrimaryDark),
            detailItem("i", "Estado", "No se pudo clasificar con precisión", QrGuardianColors.PrimaryDark),
        )
    }
}

private fun buildUrlLocalDetails(url: String): List<ResultDetailItem> {
    val details = mutableListOf<ResultDetailItem>()
    val downloadType = detectDownloadType(url)
    val path = extractUrlPath(url)

    if (downloadType.isNotBlank()) {
        details += detailItem("⬇", "Archivo", downloadType, QrGuardianColors.PrimaryDark)
    }
    if (path.isNotBlank()) {
        details += detailItem("→", "Ruta", path, QrGuardianColors.PrimaryDark)
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

private fun detectDownloadType(url: String): String {
    val path = extractUrlPath(url).lowercase()
    return when {
        path.endsWith(".apk") -> "APK"
        path.endsWith(".pdf") -> "PDF"
        path.endsWith(".zip") -> "ZIP"
        path.endsWith(".rar") -> "RAR"
        path.endsWith(".7z") -> "7z"
        path.endsWith(".doc") || path.endsWith(".docx") -> "Documento"
        path.endsWith(".xls") || path.endsWith(".xlsx") -> "Hoja de cálculo"
        path.endsWith(".ppt") || path.endsWith(".pptx") -> "Presentación"
        path.endsWith(".mp3") || path.endsWith(".wav") -> "Audio"
        path.endsWith(".mp4") || path.endsWith(".mov") || path.endsWith(".mkv") -> "Vídeo"
        path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".webp") -> "Imagen"
        else -> ""
    }
}

private fun buildRemoteDetails(section: ScanSectionResult): List<ResultDetailItem> {
    val details = mutableListOf<ResultDetailItem>()

    details += detailItem(
        leading = when (section.status) {
            ScanStatus.Completed -> "✓"
            ScanStatus.NotConfigured -> "!"
            ScanStatus.NotApplicable -> "—"
            ScanStatus.Unavailable -> "?"
        },
        label = "Estado",
        value = when (section.status) {
            ScanStatus.Completed -> "Verificación remota ejecutada"
            ScanStatus.NotConfigured -> "Sin configuración remota"
            ScanStatus.NotApplicable -> "No aplica para este contenido"
            ScanStatus.Unavailable -> "No disponible en este momento"
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
        leading = leading,
        label = label,
        value = value,
        color = color,
    )
}

private fun extractMailTarget(text: String): String {
    return text.removePrefix("mailto:").substringBefore('?').ifBlank { "No especificado" }
}

private fun extractMailExtras(text: String): String {
    val query = text.substringAfter('?', "")
    return when {
        query.isBlank() -> "Sin asunto ni cuerpo predefinido"
        query.contains("subject=") || query.contains("body=") -> "Incluye campos predefinidos"
        else -> "Parámetros presentes"
    }
}

private fun extractTelephoneTarget(text: String): String {
    return text.removePrefix("tel:").ifBlank { "No especificado" }
}

private fun extractSmsTarget(text: String): String {
    return text.removePrefix("sms:").substringBefore('?').ifBlank { "No especificado" }
}

private fun extractSmsBody(text: String): String {
    val body = text.substringAfter("body=", "")
    return if (body.isBlank()) "Sin mensaje predefinido" else body.substringBefore('&')
}

private fun extractWifiField(text: String, key: String): String {
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
        .ifBlank { "No especificado" }
}
