package com.example.mindfulshelf.domain.model

/**
 * Estados de animo que el usuario puede elegir en Home.
 *
 * Cada opcion tiene textos preparados para la UI y actua como entrada de la
 * estrategia de busqueda, convirtiendo una emocion en recomendaciones de libros.
 */
enum class MoodOption(
    val displayName: String,
    val description: String
) {
    CALM(
        displayName = "Calma",
        description = "Lecturas suaves para bajar revoluciones y respirar mejor."
    ),
    FOCUS(
        displayName = "Enfoque",
        description = "Libros que te ayudan a concentrarte y sostener la atencion."
    ),
    INSPIRATION(
        displayName = "Inspiracion",
        description = "Ideas frescas para activar creatividad y curiosidad."
    ),
    ESCAPE(
        displayName = "Desconexion",
        description = "Una pausa mental con historias y temas que te sacan del ruido."
    )
}
