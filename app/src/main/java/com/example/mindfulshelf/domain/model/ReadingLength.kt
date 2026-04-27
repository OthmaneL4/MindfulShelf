package com.example.mindfulshelf.domain.model

/**
 * Tiempo disponible que el usuario declara antes de buscar.
 *
 * Se combina con el estado de animo para generar busquedas mas utiles: una
 * pausa corta no deberia recomendar lo mismo que una sesion de lectura profunda.
 */
enum class ReadingLength(
    val label: String,
    val minutes: Int,
    val description: String
) {
    SHORT(
        label = "10 min",
        minutes = 10,
        description = "Una pausa breve, pero con sentido."
    ),
    MEDIUM(
        label = "20 min",
        minutes = 20,
        description = "Tiempo suficiente para entrar en la lectura."
    ),
    DEEP(
        label = "30+ min",
        minutes = 30,
        description = "Sesion mas inmersiva para alejarte del scroll."
    )
}
