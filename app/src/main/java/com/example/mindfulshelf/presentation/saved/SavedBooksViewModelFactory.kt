package com.example.mindfulshelf.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mindfulshelf.domain.repository.AuthRepository
import com.example.mindfulshelf.domain.repository.ReadingChallengeRepository

/**
 * Factory manual para inyectar los repositorios de autenticacion y guardados.
 *
 * Seguimos el mismo patron del resto de ViewModels para no introducir Hilt en
 * esta version final y mantener el codigo facil de defender.
 */
class SavedBooksViewModelFactory(
    private val authRepository: AuthRepository,
    private val readingChallengeRepository: ReadingChallengeRepository
) : ViewModelProvider.Factory {

    /**
     * Instancia SavedBooksViewModel con dependencias reales de sesion y Firestore.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SavedBooksViewModel(
            authRepository = authRepository,
            readingChallengeRepository = readingChallengeRepository
        ) as T
    }
}
