package com.example.mindfulshelf.data.repository

import android.util.Log
import com.example.mindfulshelf.domain.model.AuthUser
import com.example.mindfulshelf.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Implementacion de autenticacion con Firebase.
 *
 * Usa Firebase Authentication para crear/iniciar sesion y Firestore para guardar
 * un documento basico del usuario. La inicializacion real dependera de que el
 * proyecto tenga `google-services.json` configurado desde Firebase Console.
 */
class FirebaseAuthRepository : AuthRepository {

    override val currentUser: AuthUser?
        get() = runCatching {
            FirebaseAuth.getInstance().currentUser?.toAuthUser()
        }.getOrNull()

    /**
     * Autentica al usuario con email y contrasena en Firebase Authentication.
     */
    override suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthUser> = runCatching {
        val user = FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email.trim(), password)
            .await()
            .user
            ?: error("No se pudo obtener el usuario autenticado.")

        user.toAuthUser()
    }

    /**
     * Crea una cuenta nueva, actualiza el nombre visible y guarda un perfil base.
     *
     * El documento en Firestore queda preparado para futuras funciones de cuenta
     * sin mezclar esa responsabilidad con la pantalla de login.
     */
    override suspend fun createAccount(
        displayName: String,
        email: String,
        password: String
    ): Result<AuthUser> = runCatching {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val trimmedName = displayName.trim()
        val trimmedEmail = email.trim()

        val user = auth.createUserWithEmailAndPassword(trimmedEmail, password)
            .await()
            .user
            ?: error("No se pudo crear el usuario.")

        val profileRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(trimmedName)
            .build()
        user.updateProfile(profileRequest).await()

        saveUserProfileSafely(
            user = user,
            profileData = mapOf(
                "uid" to user.uid,
                "displayName" to trimmedName,
                "email" to trimmedEmail,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )

        user.toAuthUser(displayNameOverride = trimmedName)
    }

    /**
     * Convierte el token de Google en una sesion Firebase valida.
     *
     * Despues de autenticar, intenta sincronizar un perfil minimo en Firestore
     * para que el usuario tenga presencia en la base de datos de la app.
     */
    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .await()
            .user
            ?: error("No se pudo obtener el usuario autenticado con Google.")

        saveGoogleUserIfNeeded(user)
        user.toAuthUser()
    }

    /**
     * Cierra la sesion actual de Firebase Auth.
     */
    override fun signOut() {
        runCatching {
            FirebaseAuth.getInstance().signOut()
        }
    }

    /**
     * Guarda o actualiza un perfil minimo para usuarios que entran con Google.
     *
     * Usamos merge para no borrar futuros campos del usuario, por ejemplo
     * preferencias, favoritos o estadisticas de lectura.
     */
    private suspend fun saveGoogleUserIfNeeded(user: FirebaseUser) {
        saveUserProfileSafely(
            user = user,
            profileData = mapOf(
                "uid" to user.uid,
                "displayName" to user.displayName,
                "email" to user.email,
                "photoUrl" to user.photoUrl?.toString(),
                "lastLoginAt" to FieldValue.serverTimestamp()
            )
        )
    }

    /**
     * Intenta persistir el perfil del usuario sin bloquear la autenticacion.
     *
     * Firebase Auth y Firestore son servicios distintos: un usuario puede iniciar
     * sesion correctamente aunque las reglas de Firestore aun no permitan crear
     * su documento. Registramos el problema para depuracion, pero devolvemos el
     * usuario autenticado para no generar una experiencia confusa.
     */
    private suspend fun saveUserProfileSafely(
        user: FirebaseUser,
        profileData: Map<String, Any?>
    ) {
        runCatching {
            FirebaseFirestore.getInstance()
                .collection(USERS_COLLECTION)
                .document(user.uid)
                .set(profileData, SetOptions.merge())
                .await()
        }.onFailure { error ->
            Log.w(
                TAG,
                "No se pudo guardar el perfil del usuario ${user.uid} en Firestore.",
                error
            )
        }
    }

    /**
     * Convierte FirebaseUser al modelo de dominio usado por la presentacion.
     */
    private fun FirebaseUser.toAuthUser(displayNameOverride: String? = null): AuthUser {
        return AuthUser(
            uid = uid,
            displayName = displayNameOverride ?: displayName,
            email = email
        )
    }

    private companion object {
        const val TAG = "FirebaseAuthRepository"
        const val USERS_COLLECTION = "users"
    }
}
