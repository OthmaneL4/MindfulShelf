package com.example.mindfulshelf.domain.model

/**
 * Preset de busqueda derivado de la seleccion del usuario.
 *
 * Es la pieza que traduce "como me siento" y "cuanto tiempo tengo" a queries
 * concretas para Google Books. Incluye una query principal y varias de respaldo
 * para reducir resultados vacios o fallos puntuales de la API.
 */
data class BookSearchPreset(
    val mood: MoodOption,
    val readingLength: ReadingLength,
    val query: String,
    val fallbackQueries: List<String>,
    val langRestrict: String? = null,
    val title: String,
    val subtitle: String,
    val recommendationReason: String
) {
    /**
     * Orden final de busquedas que ejecutara el repositorio.
     */
    val searchQueries: List<String>
        get() = (listOf(query) + fallbackQueries).distinct()

    companion object {
        /**
         * Construye un preset completo a partir de la seleccion de Home.
         */
        fun fromSelection(
            mood: MoodOption,
            readingLength: ReadingLength
        ): BookSearchPreset {
            val moodQueries = when (mood) {
                MoodOption.CALM -> listOf(
                    "mindfulness meditation essays",
                    "calm self help books",
                    "wellbeing philosophy books"
                )

                MoodOption.FOCUS -> listOf(
                    "deep work productivity habits",
                    "focus attention learning books",
                    "productivity psychology books"
                )

                MoodOption.INSPIRATION -> listOf(
                    "creativity art biographies",
                    "creative inspiration books",
                    "innovation philosophy books"
                )

                MoodOption.ESCAPE -> listOf(
                    "travel nature adventure fiction",
                    "nature writing literary fiction",
                    "adventure novels travel books"
                )
            }

            val lengthQuery = when (readingLength) {
                ReadingLength.SHORT -> "short essays"
                ReadingLength.MEDIUM -> "practical guide"
                ReadingLength.DEEP -> "deep reading"
            }

            val title = when (mood) {
                MoodOption.CALM -> "Lecturas para recuperar la calma"
                MoodOption.FOCUS -> "Lecturas para volver al enfoque"
                MoodOption.INSPIRATION -> "Lecturas para despertar ideas"
                MoodOption.ESCAPE -> "Lecturas para desconectar del ruido"
            }

            val subtitle = when (readingLength) {
                ReadingLength.SHORT -> "Recomendaciones cortas para aprovechar un hueco sin caer en el scroll."
                ReadingLength.MEDIUM -> "Un bloque equilibrado para reconectar con una lectura con sustancia."
                ReadingLength.DEEP -> "Una sesion mas larga para entrar de verdad en otra historia o idea."
            }

            val recommendationReason = when (mood) {
                MoodOption.CALM -> "Encaja con un momento de pausa consciente y lectura serena."
                MoodOption.FOCUS -> "Encaja con una sesion para ordenar ideas y recuperar atencion."
                MoodOption.INSPIRATION -> "Encaja con una sesion para activar creatividad y perspectiva."
                MoodOption.ESCAPE -> "Encaja con una sesion para bajar el ruido mental y salir del feed."
            }

            return BookSearchPreset(
                mood = mood,
                readingLength = readingLength,
                query = "${moodQueries.first()} $lengthQuery",
                fallbackQueries = moodQueries.drop(1) + moodQueries.first(),
                title = title,
                subtitle = subtitle,
                recommendationReason = recommendationReason
            )
        }
    }
}
