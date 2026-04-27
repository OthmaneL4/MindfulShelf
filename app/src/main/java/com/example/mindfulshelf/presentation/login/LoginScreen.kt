package com.example.mindfulshelf.presentation.login

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.GetCredentialRequest
import com.example.mindfulshelf.core.toUserMessage
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

/**
 * Pantalla de autenticacion de MindfulShelf.
 *
 * Permite alternar entre iniciar sesion y crear cuenta. La pantalla mantiene la
 * logica simple: renderiza estado y envia eventos al ViewModel, que se encarga
 * de validar y hablar con Firebase.
 */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onModeSelected: (AuthMode) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogleIdTokenReceived: (String) -> Unit,
    onGoogleSignInError: (String) -> Unit,
    onSignOut: () -> Unit,
    onGoHome: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var googleRequestInProgress by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.authenticatedUser != null) {
            AuthenticatedUserCard(
                uiState = uiState,
                onGoHome = onGoHome,
                onSignOut = {
                    coroutineScope.launch {
                        runCatching {
                            credentialManager.clearCredentialState(
                                ClearCredentialStateRequest()
                            )
                        }
                        onSignOut()
                    }
                }
            )
        } else {
            AuthFormCard(
                uiState = uiState,
                onModeSelected = onModeSelected,
                onDisplayNameChanged = onDisplayNameChanged,
                onEmailChanged = onEmailChanged,
                onPasswordChanged = onPasswordChanged,
                onSubmit = onSubmit,
                googleRequestInProgress = googleRequestInProgress,
                onGoogleSignInClick = {
                    if (!uiState.isLoading && !googleRequestInProgress) {
                        googleRequestInProgress = true
                        coroutineScope.launch {
                            requestGoogleIdToken(
                                context = context,
                                credentialManager = credentialManager
                            )
                                .onSuccess(onGoogleIdTokenReceived)
                                .onFailure { error ->
                                    onGoogleSignInError(
                                        error.toUserMessage(
                                            "No pudimos abrir el inicio de sesion con Google."
                                        )
                                    )
                                }
                            googleRequestInProgress = false
                        }
                    }
                }
            )
        }
    }
}

/**
 * Formulario principal para login/registro.
 */
@Composable
private fun AuthFormCard(
    uiState: LoginUiState,
    onModeSelected: (AuthMode) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    googleRequestInProgress: Boolean,
    onGoogleSignInClick: () -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (uiState.mode == AuthMode.SIGN_IN) {
                    "Inicia sesion"
                } else {
                    "Crea tu cuenta"
                },
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Guarda tu progreso y prepara MindfulShelf para futuras funciones personalizadas.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AuthModeSelector(
                selectedMode = uiState.mode,
                onModeSelected = onModeSelected
            )

            GoogleSignInButton(
                isLoading = googleRequestInProgress,
                isEnabled = !uiState.isLoading,
                onClick = onGoogleSignInClick
            )

            AuthDivider()

            if (uiState.mode == AuthMode.SIGN_UP) {
                OutlinedTextField(
                    value = uiState.displayName,
                    onValueChange = onDisplayNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null
                    )
                },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )
                },
                label = { Text("Contrasena") },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (passwordVisible) {
                                "Ocultar contrasena"
                            } else {
                                "Mostrar contrasena"
                            }
                        )
                    }
                },
                singleLine = true
            )

            uiState.message?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (uiState.authenticatedUser == null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (uiState.mode == AuthMode.SIGN_IN) {
                            "Iniciar sesion"
                        } else {
                            "Crear cuenta"
                        }
                    )
                }
            }
        }
    }
}

/**
 * Boton de acceso con Google.
 *
 * La UI solo inicia el selector de cuentas; el token devuelto se enviara al
 * ViewModel para completar la autenticacion con Firebase.
 */
@Composable
private fun GoogleSignInButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = isEnabled && !isLoading,
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "G",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Continuar con Google")
        }
    }
}

/**
 * Separador visual entre proveedores sociales y formulario por email.
 */
@Composable
private fun AuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "o usa tu email",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

/**
 * Selector sencillo entre iniciar sesion y crear cuenta.
 */
@Composable
private fun AuthModeSelector(
    selectedMode: AuthMode,
    onModeSelected: (AuthMode) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val signInSelected = selectedMode == AuthMode.SIGN_IN
        val signUpSelected = selectedMode == AuthMode.SIGN_UP

        if (signInSelected) {
            Button(onClick = { onModeSelected(AuthMode.SIGN_IN) }) {
                Text("Iniciar sesion")
            }
        } else {
            OutlinedButton(onClick = { onModeSelected(AuthMode.SIGN_IN) }) {
                Text("Iniciar sesion")
            }
        }

        if (signUpSelected) {
            Button(onClick = { onModeSelected(AuthMode.SIGN_UP) }) {
                Text("Crear cuenta")
            }
        } else {
            OutlinedButton(onClick = { onModeSelected(AuthMode.SIGN_UP) }) {
                Text("Crear cuenta")
            }
        }
    }
}

/**
 * Solicita a Android un token de Google valido para Firebase.
 *
 * Primero intenta mostrar cuentas ya autorizadas; si no hay ninguna, abre el
 * selector completo de cuentas de Google. Esto mejora la experiencia sin forzar
 * al usuario a escribir de nuevo su correo.
 */
private suspend fun requestGoogleIdToken(
    context: Context,
    credentialManager: CredentialManager
): Result<String> = runCatching {
    val serverClientId = context.firebaseWebClientId()
    val credential = credentialManager
        .requestGoogleCredential(
            context = context,
            serverClientId = serverClientId,
            onlyAuthorizedAccounts = true
        )
        .recoverCatching {
            credentialManager.requestGoogleCredential(
                context = context,
                serverClientId = serverClientId,
                onlyAuthorizedAccounts = false
            ).getOrThrow()
        }
        .getOrThrow()

    val customCredential = credential as? CustomCredential
        ?: error("La cuenta seleccionada no devolvio una credencial compatible.")

    if (customCredential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        error("La credencial recibida no es un token de Google valido.")
    }

    try {
        GoogleIdTokenCredential
            .createFrom(customCredential.data)
            .idToken
    } catch (error: GoogleIdTokenParsingException) {
        throw IllegalStateException("No se pudo leer el token de Google.", error)
    }
}

private suspend fun CredentialManager.requestGoogleCredential(
    context: Context,
    serverClientId: String,
    onlyAuthorizedAccounts: Boolean
): Result<Credential> = runCatching {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(onlyAuthorizedAccounts)
        .setServerClientId(serverClientId)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    getCredential(
        context = context,
        request = request
    ).credential
}

/**
 * Obtiene el Web Client ID generado por el plugin de Google Services.
 *
 * Lo buscamos dinamicamente para que el proyecto siga compilando aunque el
 * archivo `google-services.json` aun no exista o este pendiente de actualizar.
 */
@Suppress("DiscouragedApi")
private fun Context.firebaseWebClientId(): String {
    val resourceId = resources.getIdentifier(
        "default_web_client_id",
        "string",
        packageName
    )

    if (resourceId == 0) {
        error(
            "Falta default_web_client_id. Descarga de nuevo google-services.json " +
                "despues de activar Google en Firebase y sincroniza Gradle."
        )
    }

    return getString(resourceId).takeIf { clientId -> clientId.isNotBlank() }
        ?: error("El Web Client ID de Firebase esta vacio.")
}

/**
 * Estado visual cuando el usuario ya esta autenticado.
 */
@Composable
private fun AuthenticatedUserCard(
    uiState: LoginUiState,
    onGoHome: () -> Unit,
    onSignOut: () -> Unit
) {
    val user = uiState.authenticatedUser ?: return

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sesion activa",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = user.displayName
                    ?: user.email
                    ?: "Usuario autenticado",
                style = MaterialTheme.typography.titleMedium
            )
            user.email?.let { email ->
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Volver al inicio",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesion")
            }
        }
    }
}
