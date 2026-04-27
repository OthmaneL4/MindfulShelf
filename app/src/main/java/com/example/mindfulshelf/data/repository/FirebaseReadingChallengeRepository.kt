package com.example.mindfulshelf.data.repository

import com.example.mindfulshelf.domain.model.BookDetail
import com.example.mindfulshelf.domain.model.SavedBook
import com.example.mindfulshelf.domain.repository.ReadingChallengeRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.tasks.await

/**
 * Repositorio Firestore para el reto anti-doomscrolling diario.
 *
 * Guardamos una unica lectura por dia en `users/{uid}/dailyReadings/{yyyy-MM-dd}`.
 * Esto mantiene el modelo sencillo: el usuario sabe que libro eligio hoy y la
 * app puede usar ese dato en futuras mejoras como rachas o historial.
 */
class FirebaseReadingChallengeRepository : ReadingChallengeRepository {

    /**
     * Comprueba si el libro aparece en la lista guardada del usuario.
     *
     * El nombre del metodo conserva el lenguaje del reto diario, pero la fuente
     * de verdad es `savedBooks`: si se quita de guardados, el detalle vuelve a
     * permitir guardarlo de nuevo.
     */
    override suspend fun isMarkedAsTodayReading(
        userId: String,
        bookId: String
    ): Result<Boolean> = runCatching {
        val snapshot = savedBookDocument(userId, bookId)
            .get()
            .await()

        snapshot.exists()
    }

    /**
     * Guarda el libro en la biblioteca personal y lo registra como lectura del dia.
     *
     * La operacion escribe en dos documentos pequenos para separar la lista de
     * guardados del historial diario, algo util si en el futuro se anaden rachas.
     */
    override suspend fun markAsTodayReading(
        userId: String,
        bookDetail: BookDetail
    ): Result<Unit> = runCatching {
        val savedBookData = bookDetail.toSavedBookData()

        savedBookDocument(userId, bookDetail.id)
            .set(savedBookData)
            .await()

        todayReadingDocument(userId)
            .set(
                mapOf(
                    BOOK_ID_FIELD to bookDetail.id,
                    "title" to bookDetail.title,
                    "authors" to bookDetail.authors,
                    "imageUrl" to bookDetail.highResImageUrl,
                    "markedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    /**
     * Recupera los libros guardados ordenados por fecha de guardado.
     */
    override suspend fun getSavedBooks(userId: String): Result<List<SavedBook>> = runCatching {
        savedBooksCollection(userId)
            .orderBy(SAVED_AT_FIELD, Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                val id = document.getString(BOOK_ID_FIELD) ?: return@mapNotNull null
                val title = document.getString("title") ?: return@mapNotNull null
                val authors = document.get("authors") as? List<*> ?: emptyList<Any>()

                SavedBook(
                    id = id,
                    title = title,
                    authors = authors.filterIsInstance<String>(),
                    imageUrl = document.getString("imageUrl")
                )
            }
    }

    /**
     * Elimina un libro de guardados y limpia la lectura diaria si apuntaba a el.
     *
     * La limpieza del reto diario es secundaria para que un permiso puntual no
     * impida que el usuario quite el libro de su biblioteca.
     */
    override suspend fun removeSavedBook(
        userId: String,
        bookId: String
    ): Result<Unit> = runCatching {
        savedBookDocument(userId, bookId)
            .delete()
            .await()

        cleanTodayReadingIfMatches(
            userId = userId,
            bookId = bookId
        )
    }

    /**
     * Prepara el mapa que se almacena en Firestore para la lista de guardados.
     */
    private fun BookDetail.toSavedBookData(): Map<String, Any?> {
        return mapOf(
            BOOK_ID_FIELD to id,
            "title" to title,
            "authors" to authors,
            "imageUrl" to highResImageUrl,
            SAVED_AT_FIELD to FieldValue.serverTimestamp()
        )
    }

    /**
     * Referencia al documento que representa la lectura del dia actual.
     */
    private fun todayReadingDocument(userId: String) =
        FirebaseFirestore.getInstance()
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(DAILY_READINGS_COLLECTION)
            .document(todayKey())

    /**
     * Referencia a la subcoleccion donde vive la biblioteca personal del usuario.
     */
    private fun savedBooksCollection(userId: String) =
        FirebaseFirestore.getInstance()
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(SAVED_BOOKS_COLLECTION)

    /**
     * Referencia estable al documento de un libro guardado concreto.
     */
    private fun savedBookDocument(
        userId: String,
        bookId: String
    ) = savedBooksCollection(userId).document(bookId.toSafeDocumentId())

    /**
     * Limpia la lectura diaria si apunta al libro eliminado.
     *
     * Esta operacion es secundaria: si Firestore deniega leer o borrar
     * `dailyReadings`, no debe impedir que el libro desaparezca de Guardados.
     */
    private suspend fun cleanTodayReadingIfMatches(
        userId: String,
        bookId: String
    ) {
        runCatching {
            val todayReadingReference = todayReadingDocument(userId)
            val todayReadingSnapshot = todayReadingReference
                .get()
                .await()

            if (todayReadingSnapshot.getString(BOOK_ID_FIELD) == bookId) {
                todayReadingReference
                    .delete()
                    .await()
            }
        }
    }

    /**
     * Firestore no permite barras en los ids de documento.
     *
     * Los ids de Google Books suelen ser seguros, pero codificamos la barra para
     * evitar fallos raros si en el futuro llega un id con ese caracter.
     */
    private fun String.toSafeDocumentId(): String {
        return replace("/", "%2F")
    }

    /**
     * Genera la clave diaria usada como id del documento de lectura.
     */
    private fun todayKey(): String {
        return SimpleDateFormat(DATE_PATTERN, Locale.US).format(Date())
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val DAILY_READINGS_COLLECTION = "dailyReadings"
        const val SAVED_BOOKS_COLLECTION = "savedBooks"
        const val BOOK_ID_FIELD = "bookId"
        const val SAVED_AT_FIELD = "savedAt"
        const val DATE_PATTERN = "yyyy-MM-dd"
    }
}
