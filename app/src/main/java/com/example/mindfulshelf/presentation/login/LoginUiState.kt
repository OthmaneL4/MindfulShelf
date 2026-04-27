package com.example.mindfulshelf.presentation.login

import com.example.mindfulshelf.domain.model.AuthUser

/**
 * Estado completo de la pantalla de autenticacion.
 *
 * Incluye campos del formulario, modo actual, carga, mensajes y usuario
 * autenticado. Mantenerlo junto simplifica el renderizado declarativo en Compose.
 */
data class LoginUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val authenticatedUser: AuthUser? = null
)
