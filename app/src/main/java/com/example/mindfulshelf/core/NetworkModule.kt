package com.example.mindfulshelf.core

import com.example.mindfulshelf.data.remote.BooksApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Configuracion de red compartida por toda la aplicacion.
 *
 * Define OkHttp y Retrofit una sola vez para reutilizar conexiones, aplicar
 * timeouts razonables y dejar activado el reintento automatico ante fallos de
 * conexion. Esto reduce errores intermitentes en la primera llamada a la API.
 */
object NetworkModule {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .retryOnConnectionFailure(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    /**
     * Servicio Retrofit reutilizable para todas las consultas a Google Books.
     */
    val booksApiService: BooksApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BooksApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BooksApiService::class.java)
    }
}
