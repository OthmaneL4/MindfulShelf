package com.example.mindfulshelf.domain.usecase

import com.example.mindfulshelf.domain.repository.BooksRepository

/**
 * Caso de uso para obtener el detalle de un libro.
 *
 * Mantiene el ViewModel enfocado en estado de UI y deja la intencion de negocio
 * expresada en una clase pequena y facil de testear.
 */
class GetBookDetailUseCase(
    private val booksRepository: BooksRepository
) {
    /**
     * Solicita el detalle sin exponer al ViewModel la fuente real de datos.
     */
    suspend operator fun invoke(bookId: String) = booksRepository.getBookDetail(bookId)
}
