package com.example.mindfulshelf.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfulshelf.core.toUserMessage
import com.example.mindfulshelf.domain.repository.AuthRepository
import com.example.mindfulshelf.domain.repository.ReadingChallengeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de la biblioteca personal del usuario.
 *
 * Comprueba si hay sesion activa, carga los libros guardados desde Firestore y
 * permite eliminarlos. Asi mantenemos la pantalla Compose enfocada en dibujar
 * estados y no en conocer detalles de Firebase.
 */
class SavedBooksViewModel(
    private val authRepository: AuthRepository,
    private val readingChallengeRepository: ReadingChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SavedBooksUiState>(SavedBooksUiState.Loading)
    val uiState: StateFlow<SavedBooksUiState> = _uiState.asStateFlow()

    init {
        loadSavedBooks()
    }

    /**
     * Carga la biblioteca del usuario o muestra el estado de login requerido.
     *
     * Se invoca al entrar en la pantalla y tambien al reintentar despues de un
     * error, por eso deja el estado en Loading antes de consultar Firestore.
     */
    fun loadSavedBooks() {
        val user = authRepository.currentUser

        if (user == null) {
            _uiState.value = SavedBooksUiState.LoginRequired
            return
        }

        _uiState.value = SavedBooksUiState.Loading

        viewModelScope.launch {
            readingChallengeRepository.getSavedBooks(user.uid)
                .onSuccess { books ->
                    _uiState.value = if (books.isEmpty()) {
                        SavedBooksUiState.Empty
                    } else {
                        SavedBooksUiState.Success(books = books)
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = SavedBooksUiState.Error(
                        throwable.toUserMessage("No pudimos cargar tus libros guardados.")
                    )
                }
        }
    }

    /**
     * Quita un libro de la biblioteca personal.
     *
     * La UI se actualiza solo cuando Firestore confirma el borrado para evitar
     * que el usuario vea un estado que no existe realmente en la nube.
     */
    fun removeSavedBook(bookId: String) {
        val user = authRepository.currentUser

        if (user == null) {
            _uiState.value = SavedBooksUiState.LoginRequired
            return
        }

        val currentState = uiState.value as? SavedBooksUiState.Success ?: return

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                removingBookId = bookId,
                message = null
            )

            readingChallengeRepository.removeSavedBook(
                userId = user.uid,
                bookId = bookId
            )
                .onSuccess {
                    val remainingBooks = currentState.books.filterNot { book -> book.id == bookId }
                    _uiState.value = if (remainingBooks.isEmpty()) {
                        SavedBooksUiState.Empty
                    } else {
                        SavedBooksUiState.Success(
                            books = remainingBooks,
                            message = "Libro eliminado de guardados."
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        (state as? SavedBooksUiState.Success)?.copy(
                            removingBookId = null,
                            message = throwable.toUserMessage(
                                "No pudimos quitar el libro de guardados."
                            )
                        ) ?: state
                    }
                }
        }
    }
}
