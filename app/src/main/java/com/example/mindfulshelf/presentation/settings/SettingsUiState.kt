package com.example.mindfulshelf.presentation.settings

import com.example.mindfulshelf.domain.model.AppThemeSettings

/**
 * Estado de la pantalla de configuracion.
 *
 * Envolvemos las preferencias en un estado propio para poder ampliar esta
 * pantalla despues con login, cuenta de usuario o mas ajustes sin cambiar la UI.
 */
data class SettingsUiState(
    val themeSettings: AppThemeSettings = AppThemeSettings()
)
