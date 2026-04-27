package com.example.mindfulshelf.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindfulshelf.domain.model.AppBackgroundStyle
import com.example.mindfulshelf.domain.model.AppCardStyle
import com.example.mindfulshelf.domain.model.AppPalette
import com.example.mindfulshelf.ui.theme.AppBackground
import com.example.mindfulshelf.ui.theme.PureWhite
import com.example.mindfulshelf.ui.theme.SkyBlueDeep
import com.example.mindfulshelf.ui.theme.SkyBlueSoft
import com.example.mindfulshelf.ui.theme.WarmAmber

/**
 * Pantalla de configuracion visual.
 *
 * Permite personalizar la app sin tocar logica de negocio. La pantalla solo
 * muestra opciones y envia eventos al ViewModel; el guardado se resuelve en el
 * repositorio local de preferencias.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onPaletteSelected: (AppPalette) -> Unit,
    onBackgroundSelected: (AppBackgroundStyle) -> Unit,
    onCardStyleSelected: (AppCardStyle) -> Unit,
    onResetTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Configuracion") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                SettingsIntroCard()
            }

            item {
                SettingsSection(
                    title = "Color principal",
                    subtitle = "Cambia los acentos, botones y elementos seleccionados."
                ) {
                    AppPalette.entries.forEach { palette ->
                        SettingOptionRow(
                            title = palette.displayName,
                            description = palette.description,
                            isSelected = uiState.themeSettings.palette == palette,
                            previewColor = palette.previewColor,
                            onClick = { onPaletteSelected(palette) }
                        )
                    }
                }
            }

            item {
                SettingsSection(
                    title = "Color de fondo",
                    subtitle = "Ajusta la atmosfera general de las pantallas."
                ) {
                    AppBackgroundStyle.entries.forEach { backgroundStyle ->
                        SettingOptionRow(
                            title = backgroundStyle.displayName,
                            description = backgroundStyle.description,
                            isSelected = uiState.themeSettings.backgroundStyle == backgroundStyle,
                            previewColor = backgroundStyle.previewColor,
                            onClick = { onBackgroundSelected(backgroundStyle) }
                        )
                    }
                }
            }

            item {
                SettingsSection(
                    title = "Estilo de cards",
                    subtitle = "Elige el tono base de las tarjetas de contenido."
                ) {
                    AppCardStyle.entries.forEach { cardStyle ->
                        SettingOptionRow(
                            title = cardStyle.displayName,
                            description = cardStyle.description,
                            isSelected = uiState.themeSettings.cardStyle == cardStyle,
                            previewColor = cardStyle.previewColor,
                            onClick = { onCardStyleSelected(cardStyle) }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onResetTheme,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Restablecer estilo original")
                }
            }
        }
    }
}

/**
 * Tarjeta introductoria que explica el objetivo de la pantalla al usuario.
 */
@Composable
private fun SettingsIntroCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Haz que MindfulShelf se sienta tuya",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Estos ajustes cambian el estilo visual de la app y se guardan automaticamente en este dispositivo.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Contenedor reutilizable para cada grupo de opciones.
 */
@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

/**
 * Fila seleccionable usada para paleta, fondo y cards.
 *
 * El indicador circular da una pista visual inmediata del color que se aplicara.
 */
@Composable
private fun SettingOptionRow(
    title: String,
    description: String,
    isSelected: Boolean,
    previewColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    Color.Transparent
                }
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(previewColor)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private val AppPalette.previewColor: Color
    get() = when (this) {
        AppPalette.SKY -> SkyBlueDeep
        AppPalette.MINT -> Color(0xFF3F8F76)
        AppPalette.SUNSET -> Color(0xFFD8734A)
    }

private val AppBackgroundStyle.previewColor: Color
    get() = when (this) {
        AppBackgroundStyle.CLOUD -> AppBackground
        AppBackgroundStyle.PAPER -> Color(0xFFFFF8EC)
        AppBackgroundStyle.BLUE_MIST -> Color(0xFFEAF7FF)
    }

private val AppCardStyle.previewColor: Color
    get() = when (this) {
        AppCardStyle.CLEAN_WHITE -> PureWhite
        AppCardStyle.SOFT_BLUE -> SkyBlueSoft
        AppCardStyle.WARM_READING -> WarmAmber.copy(alpha = 0.35f)
    }
