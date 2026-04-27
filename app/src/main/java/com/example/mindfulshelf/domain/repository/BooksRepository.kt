package com.example.mindfulshelf.domain.repository

import com.example.mindfulshelf.domain.model.Book
import com.example.mindfulshelf.domain.model.BookDetail
import com.example.mindfulshelf.domain.model.BookSearchPreset

/**
 * Contrato de acceso a libros usado por la capa de dominio.
 *
 * La presentacion depende de esta abstraccion, no de Retrofit. Asi el origen de
 * datos podria cambiarse por otra API, cache local o mocks de test sin tocar UI.
 */
interface BooksRepository {
    /**
     * Devuelve recomendaciones de libros para el preset construido en Home.
     */
    suspend fun searchBooks(preset: BookSearchPreset): Result<List<Book>>

    /**
     * Recupera la ficha ampliada de un libro concreto por su id de Google Books.
     */
    suspend fun getBookDetail(bookId: String): Result<BookDetail>
}
