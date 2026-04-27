package com.example.mindfulshelf.domain.repository

import com.example.mindfulshelf.domain.model.AuthUser

/**
 * Contrato de autenticacion de la app.
 *
 * La presentacion trabaja contra esta abstraccion para no acoplarse a Firebase.
 * En el futuro podriamos cambiar proveedor o anadir tests fake sin tocar la UI.
 */
interface AuthRepository {
    val currentUser: AuthUser?

    /**
     * Inicia sesion con email y contrasena mediante el proveedor configurado.
     */
    suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthUser>

    /**
     * Crea una cuenta nueva y devuelve el usuario autenticado.
     */
    suspend fun createAccount(
        displayName: String,
        email: String,
        password: String
    ): Result<AuthUser>

    /**
     * Completa la autenticacion con Google usando el token obtenido por Android.
     */
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>

    /**
     * Cierra la sesion local del usuario actual.
     */
    fun signOut()
}
