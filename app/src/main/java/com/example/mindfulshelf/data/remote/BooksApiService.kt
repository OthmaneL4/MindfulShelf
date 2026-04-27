package com.example.mindfulshelf.data.remote

import com.example.mindfulshelf.data.remote.dto.VolumeDto
import com.example.mindfulshelf.data.remote.dto.VolumesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato Retrofit para Google Books.
 *
 * Esta interfaz describe las llamadas HTTP que necesita MindfulShelf sin
 * mezclar detalles de red con la UI. Retrofit genera la implementacion en
 * tiempo de ejecucion a partir de estas anotaciones.
 */
interface BooksApiService {
    /**
     * Busca libros en Google Books usando una query textual.
     *
     * `langRestrict` es opcional para no bloquear resultados si Google no tiene
     * suficientes libros en un idioma concreto. La API key tambien es opcional
     * para permitir pruebas basicas, aunque se recomienda usarla.
     */
    @GET("volumes")
    suspend fun searchVolumes(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 12,
        @Query("startIndex") startIndex: Int = 0,
        @Query("printType") printType: String = "books",
        @Query("langRestrict") langRestrict: String? = null,
        @Query("key") apiKey: String? = null
    ): VolumesResponseDto

    /**
     * Obtiene la informacion ampliada de un libro concreto a partir de su id.
     */
    @GET("volumes/{bookId}")
    suspend fun getVolume(
        @Path("bookId") bookId: String,
        @Query("key") apiKey: String? = null
    ): VolumeDto

    companion object {
        const val BASE_URL = "https://www.googleapis.com/books/v1/"
    }
}
