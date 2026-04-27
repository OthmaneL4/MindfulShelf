package com.example.mindfulshelf.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mindfulshelf.domain.repository.AuthRepository
import com.example.mindfulshelf.domain.repository.ReadingChallengeRepository
import com.example.mindfulshelf.domain.usecase.GetBookDetailUseCase

/**
 * Factory manual para crear DetailViewModel con el id recibido por navegacion.
 *
 * Permite pasar parametros al ViewModel sin acoplarlo a la Activity ni crear
 * dependencias globales adicionales.
 */
class DetailViewModelFactory(
    private val bookId: String,
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val authRepository: AuthRepository,
    private val readingChallengeRepository: ReadingChallengeRepository
) : ViewModelProvider.Factory {

    /**
     * Construye el DetailViewModel con el id recibido desde Navigation Compose.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailViewModel(
            bookId = bookId,
            getBookDetailUseCase = getBookDetailUseCase,
            authRepository = authRepository,
            readingChallengeRepository = readingChallengeRepository
        ) as T
    }
}
