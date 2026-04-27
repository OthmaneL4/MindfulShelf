package com.example.mindfulshelf.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mindfulshelf.R
import com.example.mindfulshelf.domain.model.BookDetail
import com.example.mindfulshelf.ui.theme.WarmAmber

/**
 * Pantalla de detalle de un libro seleccionado.
 *
 * Muestra la portada en formato destacado, autores, categorias, metadatos y
 * sinopsis expandible. La pantalla solo renderiza el estado recibido desde el
 * ViewModel y delega acciones como volver o reintentar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    uiState: BookDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onMarkAsTodayReading: () -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when (uiState) {
            BookDetailUiState.Loading -> {
                LoadingDetailState(modifier = Modifier.padding(innerPadding))
            }

            is BookDetailUiState.Error -> {
                ErrorDetailState(
                    message = uiState.message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is BookDetailUiState.Success -> {
                DetailContent(
                    uiState = uiState,
                    onMarkAsTodayReading = onMarkAsTodayReading,
                    onLoginRequired = onLoginRequired,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

/**
 * Estado visual mientras se carga la ficha ampliada.
 */
@Composable
private fun LoadingDetailState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "Preparando la ficha completa del libro...",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Estado visual para errores de detalle.
 */
@Composable
private fun ErrorDetailState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = WarmAmber,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "No pudimos abrir este libro",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

/**
 * Contenido principal cuando la API devuelve correctamente el libro.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    uiState: BookDetailUiState.Success,
    onMarkAsTodayReading: () -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val bookDetail = uiState.bookDetail

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    AsyncImage(
                        model = bookDetail.highResImageUrl,
                        contentDescription = stringResource(R.string.book_cover),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = bookDetail.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    RowMetaLine(
                        icon = Icons.Default.Person,
                        value = if (bookDetail.authors.isNotEmpty()) {
                            bookDetail.authors.joinToString()
                        } else {
                            "Autor no disponible"
                        }
                    )
                    RowMetaLine(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        value = listOfNotNull(
                            bookDetail.publisher,
                            bookDetail.publishedDate,
                            bookDetail.pageCount?.let { "$it paginas" }
                        ).joinToString(" | ").ifBlank { "Informacion editorial no disponible" }
                    )
                    if (bookDetail.categories.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bookDetail.categories.forEach { category ->
                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = { Text(category) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SynopsisCard(description = bookDetail.description)
        }

        item {
            TodayReadingChallengeCard(
                isUserLoggedIn = uiState.isUserLoggedIn,
                isMarkedAsTodayReading = uiState.isMarkedAsTodayReading,
                isMarkingReading = uiState.isMarkingReading,
                readingMessage = uiState.readingMessage,
                onMarkAsTodayReading = onMarkAsTodayReading,
                onLoginRequired = onLoginRequired
            )
        }

        bookDetail.previewLink?.let { link ->
            item {
                PreviewLinkCard(
                    link = link,
                    onOpenLink = {
                        runCatching { uriHandler.openUri(link) }
                    }
                )
            }
        }
    }
}

/**
 * Tarjeta del reto diario anti-doomscrolling.
 *
 * Si no hay usuario autenticado, la accion lleva a login. Si existe sesion,
 * guardamos el libro en Firestore como lectura del dia para que la cuenta tenga
 * una utilidad real dentro de la app.
 */
@Composable
private fun TodayReadingChallengeCard(
    isUserLoggedIn: Boolean,
    isMarkedAsTodayReading: Boolean,
    isMarkingReading: Boolean,
    readingMessage: String?,
    onMarkAsTodayReading: () -> Unit,
    onLoginRequired: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isMarkedAsTodayReading) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Lock
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Reto anti-doomscrolling de hoy",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = if (isUserLoggedIn) {
                    "Marca este libro como tu lectura de hoy y cambia unos minutos de scroll por lectura con sentido."
                } else {
                    "Inicia sesion para guardar tu lectura diaria y darle continuidad a tu cambio de habito."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            readingMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = if (isUserLoggedIn) onMarkAsTodayReading else onLoginRequired,
                enabled = !isMarkingReading && !isMarkedAsTodayReading
            ) {
                if (isMarkingReading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = when {
                            isMarkedAsTodayReading -> "Lectura guardada"
                            isUserLoggedIn -> "Marcar como lectura de hoy"
                            else -> "Iniciar sesion para guardar"
                        }
                    )
                }
            }
        }
    }
}

/**
 * Fila reutilizable para mostrar metadatos con icono y texto.
 */
@Composable
private fun RowMetaLine(
    icon: ImageVector,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Tarjeta accionable para abrir la vista previa del libro en Google Books.
 *
 * El enlace viene de la API y apunta a una pagina externa, por eso lo abrimos
 * con el navegador del dispositivo en lugar de intentar renderizarlo dentro de
 * la app. Asi mantenemos la navegacion simple y cumplimos una utilidad real.
 */
@Composable
private fun PreviewLinkCard(
    link: String,
    onOpenLink: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Vista previa disponible",
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = onOpenLink) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Abrir en Google Books",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta de sinopsis con expansion manual para no saturar la pantalla inicial.
 */
@Composable
private fun SynopsisCard(description: String) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Sinopsis",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 7,
                overflow = TextOverflow.Ellipsis
            )
            Button(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Mostrar menos" else "Leer mas")
            }
        }
    }
}
