package com.example.mindfulshelf.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfulshelf.core.toUserMessage
import com.example.mindfulshelf.domain.model.BookSearchPreset
import com.example.mindfulshelf.domain.model.MoodOption
import com.example.mindfulshelf.domain.model.ReadingLength
import com.example.mindfulshelf.domain.usecase.GetBooksForPresetUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla principal.
 *
 * Recibe las interacciones del usuario, construye el preset de busqueda y
 * expone un StateFlow observable por Compose. No conoce detalles de Retrofit:
 * solo ejecuta el caso de uso y traduce el resultado a estados de UI.
 */
class HomeViewModel(
    private val getBooksForPresetUseCase: GetBooksForPresetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el estado de animo elegido sin lanzar todavia la busqueda.
     */
    fun onMoodSelected(moodOption: MoodOption) {
        _uiState.update { currentState ->
            currentState.copy(selectedMood = moodOption)
        }
    }

    /**
     * Actualiza el tiempo disponible elegido por el usuario.
     */
    fun onReadingLengthSelected(readingLength: ReadingLength) {
        _uiState.update { currentState ->
            currentState.copy(selectedReadingLength = readingLength)
        }
    }

    /**
     * Lanza la busqueda principal de la app.
     *
     * El flujo es: seleccion del usuario -> BookSearchPreset -> caso de uso ->
     * BooksUiState. Asi la pantalla puede representar Loading, Success, Empty o
     * Error sin mezclar la llamada de red con la UI.
     */
    fun searchBooks() {
        val selectedMood = uiState.value.selectedMood ?: return
        val selectedReadingLength = uiState.value.selectedReadingLength ?: return
        val preset = BookSearchPreset.fromSelection(selectedMood, selectedReadingLength)

        _uiState.update { currentState ->
            currentState.copy(
                currentPreset = preset,
                booksUiState = BooksUiState.Loading
            )
        }

        viewModelScope.launch {
            getBooksForPresetUseCase(preset)
                .onSuccess { books ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            currentPreset = preset,
                            booksUiState = if (books.isEmpty()) {
                                BooksUiState.Empty(
                                    "No encontramos lecturas para esta combinacion. Prueba con otro animo o mas tiempo."
                                )
                            } else {
                                BooksUiState.Success(books)
                            }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            currentPreset = preset,
                            booksUiState = BooksUiState.Error(
                                throwable.toUserMessage(
                                    "No pudimos cargar recomendaciones ahora mismo."
                                )
                            )
                        )
                    }
                }
        }
    }

    /**
     * Repite la busqueda con la seleccion actual.
     */
    fun retrySearch() {
        searchBooks()
    }
}
