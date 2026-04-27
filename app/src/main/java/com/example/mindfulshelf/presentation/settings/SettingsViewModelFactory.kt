package com.example.mindfulshelf.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mindfulshelf.data.preferences.ThemePreferencesRepository

/**
 * Factory manual para inyectar el repositorio local de preferencias.
 *
 * Mantiene el ViewModel testeable y evita crear dependencias directamente desde
 * la pantalla de Compose.
 */
class SettingsViewModelFactory(
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModelProvider.Factory {

    /**
     * Crea SettingsViewModel con acceso al repositorio local de preferencias.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(themePreferencesRepository) as T
    }
}
