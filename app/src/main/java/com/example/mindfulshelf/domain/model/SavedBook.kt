package com.example.mindfulshelf.domain.model

/**
 * Libro guardado por el usuario para leer mas adelante.
 *
 * Es un modelo ligero pensado para listas: contiene solo la informacion que
 * necesitamos para identificar el libro, mostrar una tarjeta clara y volver a
 * abrir su detalle usando el id original de Google Books.
 */
data class SavedBook(
    val id: String,
    val title: String,
    val authors: List<String>,
    val imageUrl: String?
)
