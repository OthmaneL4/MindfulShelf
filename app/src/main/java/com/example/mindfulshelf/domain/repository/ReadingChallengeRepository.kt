package com.example.mindfulshelf.domain.repository

import com.example.mindfulshelf.domain.model.BookDetail
import com.example.mindfulshelf.domain.model.SavedBook

/**
 * Contrato para persistir el reto de lectura diaria del usuario.
 *
 * La capa de presentacion solo necesita saber si el libro actual ya esta
 * marcado como lectura de hoy y pedir que se guarde. El detalle de Firestore
 * queda aislado en la capa de datos.
 */
interface ReadingChallengeRepository {
    /**
     * Comprueba si el libro ya forma parte de la lista guardada del usuario.
     */
    suspend fun isMarkedAsTodayReading(
        userId: String,
        bookId: String
    ): Result<Boolean>

    /**
     * Guarda el libro como lectura del dia y como libro disponible para leer despues.
     */
    suspend fun markAsTodayReading(
        userId: String,
        bookDetail: BookDetail
    ): Result<Unit>

    /**
     * Carga la biblioteca personal sincronizada en Firestore.
     */
    suspend fun getSavedBooks(userId: String): Result<List<SavedBook>>

    /**
     * Elimina un libro de guardados sin borrar la cuenta ni otros datos del usuario.
     */
    suspend fun removeSavedBook(
        userId: String,
        bookId: String
    ): Result<Unit>
}
