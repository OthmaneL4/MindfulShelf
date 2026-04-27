package com.example.mindfulshelf.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mindfulshelf.domain.usecase.GetBooksForPresetUseCase

/**
 * Factory manual para crear HomeViewModel con sus dependencias.
 *
 * Como no usamos Hilt en esta version, la factory permite inyectar el caso de
 * uso desde el NavHost manteniendo el ViewModel facil de probar.
 */
class HomeViewModelFactory(
    private val getBooksForPresetUseCase: GetBooksForPresetUseCase
) : ViewModelProvider.Factory {

    /**
     * Crea HomeViewModel con su caso de uso sin depender de un framework externo.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(getBooksForPresetUseCase) as T
    }
}
