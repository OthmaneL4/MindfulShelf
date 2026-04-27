package com.example.mindfulshelf.presentation.detail

import com.example.mindfulshelf.domain.model.BookDetail

/**
 * Estados de la pantalla de detalle.
 *
 * Separamos este estado del de Home porque la carga del detalle es una llamada
 * distinta a la API y puede fallar de manera independiente.
 */
sealed interface BookDetailUiState {
    data object Loading : BookDetailUiState

    data class Success(
        val bookDetail: BookDetail,
        val isUserLoggedIn: Boolean,
        val isMarkedAsTodayReading: Boolean = false,
        val isMarkingReading: Boolean = false,
        val readingMessage: String? = null
    ) : BookDetailUiState

    data class Error(val message: String) : BookDetailUiState
}
