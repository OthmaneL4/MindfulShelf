package com.example.mindfulshelf.presentation.home

import com.example.mindfulshelf.domain.model.BookSearchPreset
import com.example.mindfulshelf.domain.model.MoodOption
import com.example.mindfulshelf.domain.model.ReadingLength

/**
 * Estado completo de la pantalla Home.
 *
 * Agrupa la seleccion actual del usuario, el preset generado y el estado del
 * listado. Esto permite que Compose renderice la pantalla como una funcion pura
 * del estado, sin guardar logica de negocio dentro de los composables.
 */
data class HomeUiState(
    val selectedMood: MoodOption? = null,
    val selectedReadingLength: ReadingLength? = null,
    val currentPreset: BookSearchPreset? = null,
    val booksUiState: BooksUiState = BooksUiState.Idle
)
