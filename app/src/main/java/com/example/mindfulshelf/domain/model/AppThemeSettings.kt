package com.example.mindfulshelf.domain.model

/**
 * Preferencias visuales elegidas por el usuario en la pantalla de configuracion.
 *
 * Se mantienen en la capa de dominio porque representan una decision de la app,
 * no un detalle de Compose. La UI las consume para construir el tema visual.
 */
data class AppThemeSettings(
    val palette: AppPalette = AppPalette.SKY,
    val backgroundStyle: AppBackgroundStyle = AppBackgroundStyle.CLOUD,
    val cardStyle: AppCardStyle = AppCardStyle.CLEAN_WHITE
)

/**
 * Paleta principal de la aplicacion.
 *
 * Cambia principalmente colores de accion, seleccion y acentos visuales.
 */
enum class AppPalette(
    val displayName: String,
    val description: String
) {
    SKY(
        displayName = "Cielo mindful",
        description = "Azul claro, blanco y ambar. Es la identidad original de la app."
    ),
    MINT(
        displayName = "Menta tranquila",
        description = "Verdes suaves para una sensacion mas natural y relajada."
    ),
    SUNSET(
        displayName = "Atardecer lector",
        description = "Tonos calidos para destacar llamadas a la accion y lectura nocturna."
    )
}

/**
 * Estilo del fondo general de las pantallas.
 */
enum class AppBackgroundStyle(
    val displayName: String,
    val description: String
) {
    CLOUD(
        displayName = "Nube clara",
        description = "Fondo blanco azulado, limpio y muy legible."
    ),
    PAPER(
        displayName = "Papel calido",
        description = "Fondo crema suave para una sensacion mas editorial."
    ),
    BLUE_MIST(
        displayName = "Bruma azul",
        description = "Azul muy claro para reforzar la calma visual."
    )
}

/**
 * Estilo visual de las tarjetas.
 */
enum class AppCardStyle(
    val displayName: String,
    val description: String
) {
    CLEAN_WHITE(
        displayName = "Blanco limpio",
        description = "Cards blancas con contraste clasico."
    ),
    SOFT_BLUE(
        displayName = "Azul suave",
        description = "Cards ligeramente azuladas para integrar mejor la pantalla."
    ),
    WARM_READING(
        displayName = "Lectura calida",
        description = "Cards con tono crema para un aspecto mas acogedor."
    )
}
