package com.example.mindfulshelf.data.repository

import com.example.mindfulshelf.data.remote.BooksApiService
import com.example.mindfulshelf.data.remote.dto.VolumeDto
import com.example.mindfulshelf.data.remote.dto.VolumeInfoDto
import com.example.mindfulshelf.data.remote.dto.VolumesResponseDto
import com.example.mindfulshelf.domain.model.BookSearchPreset
import com.example.mindfulshelf.domain.model.MoodOption
import com.example.mindfulshelf.domain.model.ReadingLength
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests del repositorio real contra un servicio fake.
 *
 * Validan la parte critica de robustez: si Google Books devuelve vacio o falla
 * en una query, el repositorio debe seguir probando busquedas de respaldo.
 */
class BooksRepositoryImplTest {

    @Test
    /**
     * Comprueba que el repositorio no se detiene si la primera query no devuelve libros.
     */
    fun searchBooks_usesFallbackQueryWhenFirstSearchIsEmpty() = runTest {
        val apiService = FakeBooksApiService(
            responses = listOf(
                Result.success(VolumesResponseDto(items = emptyList())),
                Result.success(
                    VolumesResponseDto(
                        items = listOf(
                            VolumeDto(
                                id = "book-1",
                                volumeInfo = VolumeInfoDto(
                                    title = "Deep Work",
                                    authors = listOf("Cal Newport"),
                                    description = "A focused book."
                                )
                            )
                        )
                    )
                )
            )
        )
        val repository = BooksRepositoryImpl(
            booksApiService = apiService,
            apiKey = ""
        )
        val preset = BookSearchPreset(
            mood = MoodOption.FOCUS,
            readingLength = ReadingLength.MEDIUM,
            query = "empty query",
            fallbackQueries = listOf("deep work productivity habits"),
            title = "Lecturas para volver al enfoque",
            subtitle = "Un bloque equilibrado para reconectar con una lectura con sustancia.",
            recommendationReason = "Encaja con una sesion para ordenar ideas y recuperar atencion."
        )

        val result = repository.searchBooks(preset).getOrThrow()

        assertEquals(listOf("empty query", "deep work productivity habits"), apiService.searchedQueries)
        assertTrue(result.isNotEmpty())
        assertEquals("Deep Work", result.first().title)
    }

    @Test
    /**
     * Comprueba que un fallo puntual de Google Books no impide usar la query de respaldo.
     */
    fun searchBooks_usesFallbackQueryWhenFirstSearchFails() = runTest {
        val apiService = FakeBooksApiService(
            responses = listOf(
                Result.failure(IllegalStateException("Google Books temporalmente no disponible")),
                Result.success(
                    VolumesResponseDto(
                        items = listOf(
                            VolumeDto(
                                id = "book-2",
                                volumeInfo = VolumeInfoDto(
                                    title = "Focus",
                                    authors = listOf("Daniel Goleman"),
                                    description = "A book about attention."
                                )
                            )
                        )
                    )
                )
            )
        )
        val repository = BooksRepositoryImpl(
            booksApiService = apiService,
            apiKey = ""
        )
        val preset = BookSearchPreset(
            mood = MoodOption.FOCUS,
            readingLength = ReadingLength.MEDIUM,
            query = "unstable query",
            fallbackQueries = listOf("focus attention learning books"),
            title = "Lecturas para volver al enfoque",
            subtitle = "Un bloque equilibrado para reconectar con una lectura con sustancia.",
            recommendationReason = "Encaja con una sesion para ordenar ideas y recuperar atencion."
        )

        val result = repository.searchBooks(preset).getOrThrow()

        assertEquals(listOf("unstable query", "focus attention learning books"), apiService.searchedQueries)
        assertEquals("Focus", result.first().title)
    }
}

/**
 * Fake de Retrofit para probar el repositorio sin depender de internet.
 */
private class FakeBooksApiService(
    private val responses: List<Result<VolumesResponseDto>>
) : BooksApiService {

    val searchedQueries = mutableListOf<String>()

    /**
     * Devuelve respuestas prefijadas y registra las queries ejecutadas por el repositorio.
     */
    override suspend fun searchVolumes(
        query: String,
        maxResults: Int,
        startIndex: Int,
        printType: String,
        langRestrict: String?,
        apiKey: String?
    ): VolumesResponseDto {
        searchedQueries += query
        return responses.getOrElse(searchedQueries.lastIndex) {
            Result.success(VolumesResponseDto(items = emptyList()))
        }.getOrThrow()
    }

    /**
     * No forma parte del escenario de este test y falla si se invoca por error.
     */
    override suspend fun getVolume(
        bookId: String,
        apiKey: String?
    ): VolumeDto {
        error("No se usa en esta prueba.")
    }
}
