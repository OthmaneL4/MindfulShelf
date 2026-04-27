package com.example.mindfulshelf.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfulshelf.data.preferences.ThemePreferencesRepository
import com.example.mindfulshelf.domain.model.AppBackgroundStyle
import com.example.mindfulshelf.domain.model.AppCardStyle
import com.example.mindfulshelf.domain.model.AppPalette
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel de configuracion visual.
 *
 * La pantalla solo envia intenciones del usuario. El ViewModel delega la
 * persistencia al repositorio y expone un StateFlow listo para Compose.
 */
class SettingsViewModel(
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = themePreferencesRepository.settings
        .map { settings -> SettingsUiState(themeSettings = settings) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(themePreferencesRepository.settings.value)
        )

    /**
     * Guarda la paleta seleccionada por el usuario.
     */
    fun onPaletteSelected(palette: AppPalette) {
        themePreferencesRepository.updatePalette(palette)
    }

    /**
     * Guarda el fondo visual elegido en la pantalla de configuracion.
     */
    fun onBackgroundSelected(backgroundStyle: AppBackgroundStyle) {
        themePreferencesRepository.updateBackgroundStyle(backgroundStyle)
    }

    /**
     * Guarda el estilo de tarjetas seleccionado.
     */
    fun onCardStyleSelected(cardStyle: AppCardStyle) {
        themePreferencesRepository.updateCardStyle(cardStyle)
    }

    /**
     * Restablece todos los ajustes visuales a la propuesta original de la app.
     */
    fun resetTheme() {
        themePreferencesRepository.resetToDefault()
    }
}
