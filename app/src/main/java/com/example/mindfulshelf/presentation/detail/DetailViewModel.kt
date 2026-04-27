package com.example.mindfulshelf.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfulshelf.core.toUserMessage
import com.example.mindfulshelf.domain.model.BookDetail
import com.example.mindfulshelf.domain.repository.AuthRepository
import com.example.mindfulshelf.domain.repository.ReadingChallengeRepository
import com.example.mindfulshelf.domain.usecase.GetBookDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de detalle.
 *
 * Recibe el id del libro desde Navigation Compose, solicita la informacion
 * ampliada mediante el caso de uso y expone un estado simple para que la UI
 * pueda representar carga, exito o error.
 */
class DetailViewModel(
    private val bookId: String,
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val authRepository: AuthRepository,
    private val readingChallengeRepository: ReadingChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Loading)
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        loadBook()
    }

    /**
     * Carga o recarga la ficha del libro actual.
     */
    fun loadBook() {
        _uiState.value = BookDetailUiState.Loading

        viewModelScope.launch {
            getBookDetailUseCase(bookId)
                .onSuccess { bookDetail ->
                    _uiState.value = buildSuccessState(bookDetail)
                }
                .onFailure { throwable ->
                    _uiState.value = BookDetailUiState.Error(
                        throwable.toUserMessage(
                            "No pudimos cargar el detalle del libro."
                        )
                    )
                }
        }
    }

    /**
     * Marca el libro actual como lectura de hoy.
     *
     * Esta accion necesita usuario autenticado porque el dato se guarda dentro
     * de su documento de Firestore. Asi el login tiene una utilidad clara y no
     * queda como una pantalla decorativa.
     */
    fun markAsTodayReading() {
        val currentState = uiState.value as? BookDetailUiState.Success ?: return
        val user = authRepository.currentUser

        if (user == null) {
            _uiState.value = currentState.copy(
                readingMessage = "Inicia sesion para guardar tu lectura de hoy."
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                (state as? BookDetailUiState.Success)?.copy(
                    isMarkingReading = true,
                    readingMessage = null
                ) ?: state
            }

            readingChallengeRepository
                .markAsTodayReading(
                    userId = user.uid,
                    bookDetail = currentState.bookDetail
                )
                .onSuccess {
                    _uiState.update { state ->
                        (state as? BookDetailUiState.Success)?.copy(
                            isMarkedAsTodayReading = true,
                            isMarkingReading = false,
                            readingMessage = "Lectura de hoy guardada. Buen cambio contra el scroll infinito."
                        ) ?: state
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        (state as? BookDetailUiState.Success)?.copy(
                            isMarkingReading = false,
                            readingMessage = throwable.toUserMessage(
                                "No pudimos guardar la lectura de hoy."
                            )
                        ) ?: state
                    }
                }
        }
    }

    private suspend fun buildSuccessState(bookDetail: BookDetail): BookDetailUiState.Success {
        val user = authRepository.currentUser
        val isMarked = user?.let { authUser ->
            readingChallengeRepository
                .isMarkedAsTodayReading(
                    userId = authUser.uid,
                    bookId = bookDetail.id
                )
                .getOrDefault(false)
        } ?: false

        return BookDetailUiState.Success(
            bookDetail = bookDetail,
            isUserLoggedIn = user != null,
            isMarkedAsTodayReading = isMarked
        )
    }
}
