package com.example.mindfulshelf.core

import android.content.Context
import com.example.mindfulshelf.BuildConfig
import com.example.mindfulshelf.data.preferences.ThemePreferencesRepository
import com.example.mindfulshelf.data.repository.BooksRepositoryImpl
import com.example.mindfulshelf.data.repository.FirebaseAuthRepository
import com.example.mindfulshelf.data.repository.FirebaseReadingChallengeRepository
import com.example.mindfulshelf.domain.repository.AuthRepository
import com.example.mindfulshelf.domain.repository.BooksRepository
import com.example.mindfulshelf.domain.repository.ReadingChallengeRepository

/**
 * Contenedor sencillo de dependencias de la aplicacion.
 *
 * Para este proyecto final evitamos introducir un framework de inyeccion de
 * dependencias. Este objeto centraliza la creacion del repositorio y mantiene
 * el resto de la app desacoplado de Retrofit, BuildConfig y detalles de red.
 */
object AppContainer {
    private var themePreferencesRepositoryInstance: ThemePreferencesRepository? = null

    /**
     * Repositorio principal para las consultas de Google Books.
     */
    val booksRepository: BooksRepository by lazy {
        BooksRepositoryImpl(
            booksApiService = NetworkModule.booksApiService,
            apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY
        )
    }

    /**
     * Repositorio de autenticacion respaldado por Firebase Auth.
     */
    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }

    /**
     * Repositorio encargado de la lectura diaria y de la biblioteca guardada.
     */
    val readingChallengeRepository: ReadingChallengeRepository by lazy {
        FirebaseReadingChallengeRepository()
    }

    /**
     * Repositorio de preferencias visuales inicializado en el arranque de la app.
     */
    val themePreferencesRepository: ThemePreferencesRepository
        get() = checkNotNull(themePreferencesRepositoryInstance) {
            "AppContainer.initialize(context) debe llamarse antes de acceder a las preferencias."
        }

    /**
     * Inicializa dependencias que necesitan Context.
     *
     * Usamos applicationContext para evitar fugas de memoria asociadas a una
     * Activity concreta.
     */
    fun initialize(context: Context) {
        if (themePreferencesRepositoryInstance == null) {
            themePreferencesRepositoryInstance = ThemePreferencesRepository(
                context = context.applicationContext
            )
        }
    }
}
