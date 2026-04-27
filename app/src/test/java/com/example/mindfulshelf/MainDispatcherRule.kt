package com.example.mindfulshelf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Regla de test para controlar Dispatchers.Main.
 *
 * Los ViewModels usan viewModelScope, que depende del dispatcher principal de
 * Android. En tests unitarios lo sustituimos por un dispatcher de prueba para
 * ejecutar corrutinas de forma determinista.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    /**
     * Sustituye Dispatchers.Main antes de cada prueba.
     */
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    /**
     * Restaura el dispatcher principal real al finalizar cada prueba.
     */
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
