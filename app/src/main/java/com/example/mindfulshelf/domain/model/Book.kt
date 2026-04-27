package com.example.mindfulshelf.domain.model

/**
 * Modelo de dominio para un libro mostrado en el listado de recomendaciones.
 *
 * Contiene solo los datos que Home necesita renderizar. Esto evita que la UI
 * dependa de los DTOs de Google Books y facilita cambiar la API en el futuro.
 */
data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnailUrl: String?,
    val snippet: String,
    val recommendationReason: String
)
