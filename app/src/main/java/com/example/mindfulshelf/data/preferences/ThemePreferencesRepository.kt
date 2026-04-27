package com.example.mindfulshelf.data.preferences

import android.content.Context
import com.example.mindfulshelf.domain.model.AppBackgroundStyle
import com.example.mindfulshelf.domain.model.AppCardStyle
import com.example.mindfulshelf.domain.model.AppPalette
import com.example.mindfulshelf.domain.model.AppThemeSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repositorio local para guardar preferencias visuales.
 *
 * Para esta primera version usamos SharedPreferences porque es sencillo,
 * transparente y suficiente para los valores pequenos. La clase expone un
 * StateFlow para que Compose pueda reaccionar al cambio de tema al instante.
 */
class ThemePreferencesRepository(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<AppThemeSettings> = _settings.asStateFlow()

    /**
     * Actualiza la paleta principal y notifica a Compose inmediatamente.
     *
     * Guardamos el valor en disco y despues actualizamos el StateFlow para que
     * la UI cambie sin reiniciar la aplicacion.
     */
    fun updatePalette(palette: AppPalette) {
        sharedPreferences.edit()
            .putString(KEY_PALETTE, palette.name)
            .apply()

        _settings.update { currentSettings ->
            currentSettings.copy(palette = palette)
        }
    }

    /**
     * Cambia el estilo de fondo seleccionado en Configuracion.
     *
     * Separar fondo, tarjetas y paleta permite personalizar la app sin mezclar
     * responsabilidades ni crear un tema nuevo para cada combinacion.
     */
    fun updateBackgroundStyle(backgroundStyle: AppBackgroundStyle) {
        sharedPreferences.edit()
            .putString(KEY_BACKGROUND, backgroundStyle.name)
            .apply()

        _settings.update { currentSettings ->
            currentSettings.copy(backgroundStyle = backgroundStyle)
        }
    }

    /**
     * Persiste el estilo visual de las tarjetas.
     *
     * Este valor afecta principalmente a superficies de lectura y cards, por eso
     * se guarda de forma independiente a la paleta general.
     */
    fun updateCardStyle(cardStyle: AppCardStyle) {
        sharedPreferences.edit()
            .putString(KEY_CARD, cardStyle.name)
            .apply()

        _settings.update { currentSettings ->
            currentSettings.copy(cardStyle = cardStyle)
        }
    }

    /**
     * Restaura la identidad visual por defecto de MindfulShelf.
     *
     * Se limpia SharedPreferences y se emite un estado nuevo para que la pantalla
     * de configuracion refleje el cambio al instante.
     */
    fun resetToDefault() {
        sharedPreferences.edit().clear().apply()
        _settings.value = AppThemeSettings()
    }

    /**
     * Lee las preferencias actuales al iniciar la app.
     *
     * Si algun valor guardado ya no existe, se usa un valor seguro por defecto
     * para evitar errores tras cambios futuros en los enums.
     */
    private fun readSettings(): AppThemeSettings {
        return AppThemeSettings(
            palette = readEnum(KEY_PALETTE, AppPalette.SKY),
            backgroundStyle = readEnum(KEY_BACKGROUND, AppBackgroundStyle.CLOUD),
            cardStyle = readEnum(KEY_CARD, AppCardStyle.CLEAN_WHITE)
        )
    }

    /**
     * Recupera un enum guardado como texto de forma tolerante a cambios.
     *
     * Es intencionadamente generica porque los tres ajustes visuales siguen el
     * mismo patron de persistencia.
     */
    private inline fun <reified T : Enum<T>> readEnum(
        key: String,
        defaultValue: T
    ): T {
        val storedValue = sharedPreferences.getString(key, null) ?: return defaultValue
        return enumValues<T>().firstOrNull { enumValue ->
            enumValue.name == storedValue
        } ?: defaultValue
    }

    private companion object {
        const val PREFERENCES_NAME = "mindful_shelf_theme_preferences"
        const val KEY_PALETTE = "palette"
        const val KEY_BACKGROUND = "background"
        const val KEY_CARD = "card"
    }
}
