package com.example.mindfulshelf.domain.model

/**
 * Modelo de dominio para la pantalla de detalle.
 *
 * Agrupa la informacion ampliada del libro: sinopsis, autores, portada de
 * mayor calidad y metadatos editoriales. La pantalla Detail renderiza este
 * modelo sin conocer la respuesta original de Google Books.
 */
data class BookDetail(
    val id: String,
    val title: String,
    val authors: List<String>,
    val description: String,
    val highResImageUrl: String?,
    val categories: List<String>,
    val publisher: String?,
    val publishedDate: String?,
    val pageCount: Int?,
    val previewLink: String?
)
