package com.example.mindfulshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mindfulshelf.core.AppContainer
import com.example.mindfulshelf.presentation.navigation.MindfulShelfNavHost
import com.example.mindfulshelf.ui.theme.MindfulShelfTheme

/**
 * Punto de entrada de la aplicacion Android.
 *
 * Su responsabilidad es minima a proposito: prepara el modo edge-to-edge,
 * aplica el tema visual de MindfulShelf y delega toda la navegacion al NavHost.
 * Mantener esta clase pequena facilita que la arquitectura MVVM viva en las
 * capas de presentacion, dominio y datos, no dentro de la Activity.
 */
class MainActivity : ComponentActivity() {
    /**
     * Configura las dependencias globales y monta la interfaz declarativa.
     *
     * La Activity no contiene reglas de negocio: inicializa el contenedor,
     * aplica el tema elegido por el usuario y entrega el control al grafo de
     * navegacion principal.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            val themeSettings by AppContainer.themePreferencesRepository.settings
                .collectAsStateWithLifecycle()

            MindfulShelfTheme(
                themeSettings = themeSettings,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MindfulShelfNavHost(
                        booksRepository = AppContainer.booksRepository,
                        authRepository = AppContainer.authRepository,
                        readingChallengeRepository = AppContainer.readingChallengeRepository,
                        themePreferencesRepository = AppContainer.themePreferencesRepository
                    )
                }
            }
        }
    }
}
