package com.example.mindfulshelf.domain.model

/**
 * Usuario autenticado dentro de MindfulShelf.
 *
 * Es un modelo de dominio propio para que la UI no dependa directamente de
 * FirebaseUser. Asi mantenemos Firebase dentro de la capa de datos.
 */
data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?
)
