package com.example.mindfulshelf.presentation.detail

import com.example.mindfulshelf.MainDispatcherRule
import com.example.mindfulshelf.domain.model.AuthUser
import com.example.mindfulshelf.domain.model.Book
import com.example.mindfulshelf.domain.model.BookDetail
import com.example.mindfulshelf.domain.model.BookSearchPreset
import com.example.mindfulshelf.domain.model.MoodOption
import com.example.mindfulshelf.domain.model.ReadingLength
import com.example.mindfulshelf.domain.model.SavedBook
import com.example.mindfulshelf.domain.repository.AuthRepository
import com.example.mindfulshelf.domain.repository.BooksRepository
import com.example.mindfulshelf.domain.repository.ReadingChallengeRepository
import com.example.mindfulshelf.domain.usecase.GetBookDetailUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests del ViewModel de detalle.
 *
 * Verifican que la carga inicial transforma correctamente la respuesta del
 * repositorio en estados de UI para la pantalla Detail.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * Verifica que un detalle valido termina en estado Success.
     */
    @Test
    fun loadBook_emitsSuccessWhenRepositoryReturnsDetail() = runTest {
        val repository = DetailFakeBooksRepository(
            detailResult = Result.success(
                BookDetail(
                    id = "abc",
                    title = "Atomic Habits",
                    authors = listOf("James Clear"),
                    description = "Habitos pequenos para cambios grandes.",
                    highResImageUrl = null,
                    categories = listOf("Productividad"),
                    publisher = "Ariel",
                    publishedDate = "2020",
                    pageCount = 320,
                    previewLink = null
                )
            )
        )

        val viewModel = DetailViewModel(
            bookId = "abc",
            getBookDetailUseCase = GetBookDetailUseCase(repository),
            authRepository = DetailFakeAuthRepository(),
            readingChallengeRepository = DetailFakeReadingChallengeRepository()
        )

        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is BookDetailUiState.Success)
        finalState as BookDetailUiState.Success
        assertEquals("Atomic Habits", finalState.bookDetail.title)
    }

    /**
     * Verifica que un fallo al cargar el libro termina en estado Error.
     */
    @Test
    fun loadBook_emitsErrorWhenRepositoryFails() = runTest {
        val repository = DetailFakeBooksRepository(
            detailResult = Result.failure(IllegalArgumentException("Libro no disponible"))
        )

        val viewModel = DetailViewModel(
            bookId = "missing",
            getBookDetailUseCase = GetBookDetailUseCase(repository),
            authRepository = DetailFakeAuthRepository(),
            readingChallengeRepository = DetailFakeReadingChallengeRepository()
        )

        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is BookDetailUiState.Error)
        finalState as BookDetailUiState.Error
        assertTrue(finalState.message.contains("Libro no disponible"))
    }
}

/**
 * Repositorio fake de autenticacion para aislar el test de Firebase.
 */
private class DetailFakeAuthRepository(
    override val currentUser: AuthUser? = null
) : AuthRepository {
    /**
     * No se usa en estas pruebas; existe para completar el contrato.
     */
    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return Result.failure(UnsupportedOperationException("No se usa en este test."))
    }

    /**
     * No se usa en estas pruebas; existe para completar el contrato.
     */
    override suspend fun createAccount(
        displayName: String,
        email: String,
        password: String
    ): Result<AuthUser> {
        return Result.failure(UnsupportedOperationException("No se usa en este test."))
    }

    /**
     * No se usa en estas pruebas; existe para completar el contrato.
     */
    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        return Result.failure(UnsupportedOperationException("No se usa en este test."))
    }

    /**
     * En el fake no hay estado real de sesion que limpiar.
     */
    override fun signOut() = Unit
}

/**
 * Repositorio fake del reto diario para no depender de Firestore en tests.
 */
private class DetailFakeReadingChallengeRepository : ReadingChallengeRepository {
    /**
     * Siempre responde que el libro no esta marcado para no contaminar el escenario.
     */
    override suspend fun isMarkedAsTodayReading(
        userId: String,
        bookId: String
    ): Result<Boolean> {
        return Result.success(false)
    }

    /**
     * Simula un guardado correcto sin usar Firestore real.
     */
    override suspend fun markAsTodayReading(
        userId: String,
        bookDetail: BookDetail
    ): Result<Unit> {
        return Result.success(Unit)
    }

    /**
     * Devuelve una biblioteca vacia porque no es relevante para estas pruebas.
     */
    override suspend fun getSavedBooks(userId: String): Result<List<SavedBook>> {
        return Result.success(emptyList())
    }

    /**
     * Simula la eliminacion correcta de un libro guardado.
     */
    override suspend fun removeSavedBook(
        userId: String,
        bookId: String
    ): Result<Unit> {
        return Result.success(Unit)
    }
}

/**
 * Repositorio fake centrado en la carga de detalle.
 */
private class DetailFakeBooksRepository(
    private val detailResult: Result<BookDetail>
) : BooksRepository {
    /**
     * Devuelve una lista vacia porque los tests de detalle no dependen de Home.
     */
    override suspend fun searchBooks(preset: BookSearchPreset): Result<List<Book>> {
        return Result.success(emptyList())
    }

    /**
     * Devuelve el detalle prefijado para controlar el escenario del test.
     */
    override suspend fun getBookDetail(bookId: String): Result<BookDetail> {
        return detailResult
    }
}
