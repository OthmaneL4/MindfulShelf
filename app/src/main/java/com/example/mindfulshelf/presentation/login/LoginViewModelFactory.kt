package com.example.mindfulshelf.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mindfulshelf.domain.repository.AuthRepository

/**
 * Factory manual para crear LoginViewModel con AuthRepository.
 */
class LoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    /**
     * Crea el ViewModel de login con el repositorio de autenticacion configurado.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(authRepository) as T
    }
}
