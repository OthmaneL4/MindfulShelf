package com.example.mindfulshelf.data.repository

import com.example.mindfulshelf.data.remote.BooksApiService
import com.example.mindfulshelf.data.remote.dto.toBook
import com.example.mindfulshelf.data.remote.dto.toBookDetail
import com.example.mindfulshelf.domain.model.Book
import com.example.mindfulshelf.domain.model.BookDetail
import com.example.mindfulshelf.domain.model.BookSearchPreset
import com.example.mindfulshelf.domain.repository.BooksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementacion concreta del repositorio de libros.
 *
 * Esta clase es el puente entre dominio y red: recibe presets de busqueda,
 * llama a Google Books, aplica estrategias de fallback y transforma DTOs en
 * modelos de dominio. La UI no conoce Retrofit ni la estructura remota.
 */
class BooksRepositoryImpl(
    private val booksApiService: BooksApiService,
    private val apiKey: String
) : BooksRepository {

    /**
     * Ejecuta una busqueda resiliente.
     *
     * En vez de depender de una sola query, prueba varias busquedas relacionadas
     * con el estado de animo y acumula resultados unicos. Si una llamada falla
     * temporalmente, continua con la siguiente para mejorar la experiencia.
     */
    override suspend fun searchBooks(preset: BookSearchPreset): Result<List<Book>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val optionalApiKey = apiKey.takeIf { it.isNotBlank() }
                val collectedBooks = linkedMapOf<String, Book>()
                var lastError: Throwable? = null

                for (query in preset.searchQueries) {
                    val books = try {
                        booksApiService.searchVolumes(
                            query = query,
                            maxResults = RESULTS_PER_QUERY,
                            langRestrict = preset.langRestrict,
                            apiKey = optionalApiKey
                        ).items.orEmpty()
                            .mapNotNull { volume -> volume.toBook(preset.recommendationReason) }
                    } catch (error: Throwable) {
                        // Si Google Books falla de forma temporal, seguimos con las busquedas de respaldo.
                        lastError = error
                        emptyList()
                    }

                    books.forEach { book ->
                        collectedBooks.putIfAbsent(book.id, book)
                    }

                    if (collectedBooks.size >= MAX_RESULTS) {
                        return@runCatching collectedBooks.values.take(MAX_RESULTS)
                    }
                }

                if (collectedBooks.isEmpty() && lastError != null) {
                    throw lastError
                }

                collectedBooks.values.toList()
            }
        }
    }

    /**
     * Recupera la ficha ampliada de un libro seleccionado en Home.
     */
    override suspend fun getBookDetail(bookId: String): Result<BookDetail> {
        return withContext(Dispatchers.IO) {
            runCatching {
                booksApiService.getVolume(
                    bookId = bookId,
                    apiKey = apiKey.takeIf { it.isNotBlank() }
                ).toBookDetail() ?: error("No pudimos interpretar la informacion del libro.")
            }
        }
    }

    private companion object {
        const val RESULTS_PER_QUERY = 8
        const val MAX_RESULTS = 12
    }
}
