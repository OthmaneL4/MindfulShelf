package com.example.mindfulshelf.domain.usecase

import com.example.mindfulshelf.domain.model.BookSearchPreset
import com.example.mindfulshelf.domain.repository.BooksRepository

/**
 * Caso de uso para buscar recomendaciones segun un preset de animo y tiempo.
 *
 * Representa la accion principal de MindfulShelf: convertir una pausa del
 * usuario en una lista de lecturas utiles.
 */
class GetBooksForPresetUseCase(
    private val booksRepository: BooksRepository
) {
    /**
     * Ejecuta la busqueda manteniendo al ViewModel independiente del repositorio.
     */
    suspend operator fun invoke(preset: BookSearchPreset) = booksRepository.searchBooks(preset)
}
