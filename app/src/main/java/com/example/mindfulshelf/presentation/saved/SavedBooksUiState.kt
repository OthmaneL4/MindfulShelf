package com.example.mindfulshelf.presentation.saved

import com.example.mindfulshelf.domain.model.SavedBook

/**
 * Estados de la pantalla de libros guardados.
 *
 * Incluimos LoginRequired porque esta seccion solo tiene sentido con una cuenta:
 * los libros se guardan en Firestore bajo el usuario autenticado.
 */
sealed interface SavedBooksUiState {
    data object Loading : SavedBooksUiState

    data object LoginRequired : SavedBooksUiState

    data object Empty : SavedBooksUiState

    data class Success(
        val books: List<SavedBook>,
        val removingBookId: String? = null,
        val message: String? = null
    ) : SavedBooksUiState

    data class Error(val message: String) : SavedBooksUiState
}
