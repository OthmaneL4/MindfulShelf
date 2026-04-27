package com.example.mindfulshelf.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.mindfulshelf.domain.model.AppBackgroundStyle
import com.example.mindfulshelf.domain.model.AppCardStyle
import com.example.mindfulshelf.domain.model.AppPalette
import com.example.mindfulshelf.domain.model.AppThemeSettings

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = NavyText,
    secondary = WarmAmber,
    onSecondary = NavyText,
    tertiary = SkyBlueSoft,
    background = NavyText,
    onBackground = PureWhite,
    surface = NavySurface,
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF315D85),
    onSurfaceVariant = PureWhite,
    error = ErrorRose,
    onError = PureWhite
)

/**
 * Tema Compose de la aplicacion.
 *
 * Desactiva dynamic color por defecto para respetar la identidad visual del
 * proyecto en cualquier dispositivo y aplica las preferencias visuales que el
 * usuario puede cambiar desde Configuracion.
 */
@Composable
fun MindfulShelfTheme(
    themeSettings: AppThemeSettings = AppThemeSettings(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Se mantiene desactivado por defecto para conservar la identidad visual de la app.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> themeSettings.toLightColorScheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Traduce las preferencias del usuario al esquema Material 3 claro.
 *
 * Mantener esta conversion en una extension evita duplicar colores por toda la
 * UI y deja el sistema visual centralizado en la capa de tema.
 */
private fun AppThemeSettings.toLightColorScheme() = lightColorScheme(
    primary = palette.primaryColor,
    onPrimary = PureWhite,
    secondary = palette.secondaryColor,
    onSecondary = NavyText,
    tertiary = palette.tertiaryColor,
    onTertiary = NavyText,
    background = backgroundStyle.backgroundColor,
    onBackground = NavyText,
    surface = cardStyle.cardColor,
    onSurface = NavyText,
    surfaceVariant = palette.surfaceVariantColor,
    onSurfaceVariant = NavySurface,
    outline = palette.outlineColor,
    error = ErrorRose,
    onError = PureWhite
)

private val AppPalette.primaryColor: Color
    get() = when (this) {
        AppPalette.SKY -> SkyBlueDeep
        AppPalette.MINT -> Color(0xFF3F8F76)
        AppPalette.SUNSET -> Color(0xFFD8734A)
    }

private val AppPalette.secondaryColor: Color
    get() = when (this) {
        AppPalette.SKY -> WarmAmber
        AppPalette.MINT -> Color(0xFFE5C46A)
        AppPalette.SUNSET -> Color(0xFFF4B95D)
    }

private val AppPalette.tertiaryColor: Color
    get() = when (this) {
        AppPalette.SKY -> SkyBlueSoft
        AppPalette.MINT -> Color(0xFFDDF4EA)
        AppPalette.SUNSET -> Color(0xFFFFE7D8)
    }

private val AppPalette.surfaceVariantColor: Color
    get() = when (this) {
        AppPalette.SKY -> MistBlue
        AppPalette.MINT -> Color(0xFFE8F7F0)
        AppPalette.SUNSET -> Color(0xFFFFF0E3)
    }

private val AppPalette.outlineColor: Color
    get() = when (this) {
        AppPalette.SKY -> SkyBlue
        AppPalette.MINT -> Color(0xFF76C7AA)
        AppPalette.SUNSET -> Color(0xFFEEA17E)
    }

private val AppBackgroundStyle.backgroundColor: Color
    get() = when (this) {
        AppBackgroundStyle.CLOUD -> AppBackground
        AppBackgroundStyle.PAPER -> Color(0xFFFFF8EC)
        AppBackgroundStyle.BLUE_MIST -> Color(0xFFEAF7FF)
    }

private val AppCardStyle.cardColor: Color
    get() = when (this) {
        AppCardStyle.CLEAN_WHITE -> PureWhite
        AppCardStyle.SOFT_BLUE -> Color(0xFFF0FAFF)
        AppCardStyle.WARM_READING -> Color(0xFFFFF4E1)
    }
