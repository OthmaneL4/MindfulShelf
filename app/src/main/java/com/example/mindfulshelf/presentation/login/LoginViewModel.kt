package com.example.mindfulshelf.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfulshelf.core.toUserMessage
import com.example.mindfulshelf.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del flujo de login/registro.
 *
 * Centraliza validacion sencilla, llamada al repositorio Firebase y conversion
 * de resultados en estado de UI. La pantalla no conoce Firebase ni corrutinas.
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(authenticatedUser = authRepository.currentUser)
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Cambia entre iniciar sesion y crear cuenta.
     *
     * Al cambiar de modo se limpia el mensaje anterior para que no se arrastren
     * errores de un formulario al otro.
     */
    fun onModeSelected(mode: AuthMode) {
        _uiState.update { currentState ->
            currentState.copy(mode = mode, message = null)
        }
    }

    /**
     * Actualiza el nombre mostrado durante el registro.
     */
    fun onDisplayNameChanged(displayName: String) {
        _uiState.update { currentState ->
            currentState.copy(displayName = displayName, message = null)
        }
    }

    /**
     * Actualiza el email escrito por el usuario y limpia mensajes obsoletos.
     */
    fun onEmailChanged(email: String) {
        _uiState.update { currentState ->
            currentState.copy(email = email, message = null)
        }
    }

    /**
     * Actualiza la contrasena del formulario.
     */
    fun onPasswordChanged(password: String) {
        _uiState.update { currentState ->
            currentState.copy(password = password, message = null)
        }
    }

    /**
     * Valida el formulario y ejecuta login o registro segun el modo actual.
     */
    fun submit() {
        val currentState = uiState.value
        val validationMessage = currentState.validate()

        if (validationMessage != null) {
            _uiState.update { state -> state.copy(message = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true, message = null) }

            val result = if (currentState.mode == AuthMode.SIGN_IN) {
                authRepository.signIn(
                    email = currentState.email,
                    password = currentState.password
                )
            } else {
                authRepository.createAccount(
                    displayName = currentState.displayName,
                    email = currentState.email,
                    password = currentState.password
                )
            }

            result
                .onSuccess { user ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            authenticatedUser = user,
                            password = "",
                            message = "Sesion iniciada correctamente."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            message = error.toUserMessage(
                                "No pudimos completar la autenticacion. Revisa Firebase y vuelve a intentarlo."
                            )
                        )
                    }
                }
        }
    }

    /**
     * Completa el acceso con Google usando el token recibido desde la pantalla.
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true, message = null) }

            authRepository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            authenticatedUser = user,
                            password = "",
                            message = "Sesion iniciada con Google correctamente."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            message = error.toUserMessage(
                                "No pudimos iniciar sesion con Google. Revisa Firebase y vuelve a intentarlo."
                            )
                        )
                    }
                }
        }
    }

    /**
     * Muestra errores producidos antes de llegar a Firebase, como cancelaciones
     * o problemas al obtener credenciales de Google.
     */
    fun showAuthMessage(message: String) {
        _uiState.update { state ->
            state.copy(isLoading = false, message = message)
        }
    }

    /**
     * Cierra la sesion y devuelve la pantalla al estado inicial.
     */
    fun signOut() {
        authRepository.signOut()
        _uiState.value = LoginUiState(message = "Sesion cerrada correctamente.")
    }

    /**
     * Aplica validaciones de formulario antes de llamar a Firebase.
     *
     * Mantener estas reglas aqui permite que la UI sea declarativa y que el
     * repositorio reciba datos con una forma minima aceptable.
     */
    private fun LoginUiState.validate(): String? {
        if (mode == AuthMode.SIGN_UP && displayName.trim().length < 2) {
            return "Introduce un nombre de al menos 2 caracteres."
        }

        if (!email.contains("@") || !email.contains(".")) {
            return "Introduce un email valido."
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            return "La contrasena debe tener al menos 6 caracteres."
        }

        return null
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
