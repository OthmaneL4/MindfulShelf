package com.example.mindfulshelf.presentation.home

import com.example.mindfulshelf.domain.model.Book

/**
 * Estados posibles del listado de recomendaciones.
 *
 * Usar una sealed interface obliga a la UI a contemplar explicitamente carga,
 * exito, vacio y error, cumpliendo el requisito de gestion robusta de estados.
 */
sealed interface BooksUiState {
    data object Idle : BooksUiState

    data object Loading : BooksUiState

    data class Success(val books: List<Book>) : BooksUiState

    data class Empty(val message: String) : BooksUiState

    data class Error(val message: String) : BooksUiState
}
