package com.example.mindfulshelf.presentation.home

import com.example.mindfulshelf.MainDispatcherRule
import com.example.mindfulshelf.domain.model.Book
import com.example.mindfulshelf.domain.model.BookDetail
import com.example.mindfulshelf.domain.model.BookSearchPreset
import com.example.mindfulshelf.domain.model.MoodOption
import com.example.mindfulshelf.domain.model.ReadingLength
import com.example.mindfulshelf.domain.repository.BooksRepository
import com.example.mindfulshelf.domain.usecase.GetBooksForPresetUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests del ViewModel de Home.
 *
 * Comprueban que la pantalla recibe estados correctos segun el resultado del
 * caso de uso: Success, Empty y Error.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * Verifica la transicion completa desde Loading hasta Success.
     */
    @Test
    fun searchBooks_emitsLoadingThenSuccess() = runTest {
        val repository = FakeBooksRepository(
            searchResult = Result.success(
                listOf(
                    Book(
                        id = "1",
                        title = "Deep Work",
                        authors = listOf("Cal Newport"),
                        thumbnailUrl = null,
                        snippet = "Un libro sobre enfoque.",
                        recommendationReason = "Encaja con una sesion de enfoque."
                    )
                )
            ),
            searchDelayMs = 100
        )
        val viewModel = HomeViewModel(GetBooksForPresetUseCase(repository))

        viewModel.onMoodSelected(MoodOption.FOCUS)
        viewModel.onReadingLengthSelected(ReadingLength.MEDIUM)
        viewModel.searchBooks()

        assertEquals(BooksUiState.Loading, viewModel.uiState.value.booksUiState)

        advanceUntilIdle()

        val finalState = viewModel.uiState.value.booksUiState
        assertTrue(finalState is BooksUiState.Success)
        finalState as BooksUiState.Success
        assertEquals("Deep Work", finalState.books.first().title)
    }

    /**
     * Comprueba que Home muestra estado vacio cuando no llegan libros.
     */
    @Test
    fun searchBooks_emitsEmptyWhenRepositoryReturnsNoBooks() = runTest {
        val repository = FakeBooksRepository(searchResult = Result.success(emptyList()))
        val viewModel = HomeViewModel(GetBooksForPresetUseCase(repository))

        viewModel.onMoodSelected(MoodOption.CALM)
        viewModel.onReadingLengthSelected(ReadingLength.SHORT)
        viewModel.searchBooks()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.booksUiState is BooksUiState.Empty)
    }

    /**
     * Comprueba que los errores del repositorio se traducen al estado Error.
     */
    @Test
    fun searchBooks_emitsErrorWhenRepositoryFails() = runTest {
        val repository = FakeBooksRepository(
            searchResult = Result.failure(IllegalStateException("Sin conexion"))
        )
        val viewModel = HomeViewModel(GetBooksForPresetUseCase(repository))

        viewModel.onMoodSelected(MoodOption.ESCAPE)
        viewModel.onReadingLengthSelected(ReadingLength.DEEP)
        viewModel.searchBooks()

        advanceUntilIdle()

        val finalState = viewModel.uiState.value.booksUiState
        assertTrue(finalState is BooksUiState.Error)
        finalState as BooksUiState.Error
        assertTrue(finalState.message.contains("Sin conexion"))
    }
}

/**
 * Repositorio fake para simular respuestas de Home sin llamar a Google Books.
 */
private class FakeBooksRepository(
    private val searchResult: Result<List<Book>>,
    private val detailResult: Result<BookDetail> = Result.failure(IllegalStateException("unused")),
    private val searchDelayMs: Long = 0L
) : BooksRepository {
    /**
     * Simula la busqueda principal con un posible retardo para probar Loading.
     */
    override suspend fun searchBooks(preset: BookSearchPreset): Result<List<Book>> {
        delay(searchDelayMs)
        return searchResult
    }

    /**
     * Devuelve un resultado prefijado cuando algun test necesite detalle.
     */
    override suspend fun getBookDetail(bookId: String): Result<BookDetail> {
        return detailResult
    }
}
