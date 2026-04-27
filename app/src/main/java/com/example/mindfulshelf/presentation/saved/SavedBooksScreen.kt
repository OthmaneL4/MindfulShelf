package com.example.mindfulshelf.presentation.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mindfulshelf.domain.model.SavedBook
import com.example.mindfulshelf.ui.theme.WarmAmber

/**
 * Pantalla de biblioteca personal.
 *
 * Muestra los libros que el usuario ha guardado desde el reto diario. Si no hay
 * sesion, explica por que debe iniciar sesion antes de poder sincronizar sus
 * lecturas.
 */
@Composable
fun SavedBooksScreen(
    uiState: SavedBooksUiState,
    onLoginClick: () -> Unit,
    onRetry: () -> Unit,
    onBookClick: (String) -> Unit,
    onRemoveBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        SavedBooksUiState.Loading -> SavedBooksLoading(modifier)
        SavedBooksUiState.LoginRequired -> SavedBooksLoginRequired(
            onLoginClick = onLoginClick,
            modifier = modifier
        )
        SavedBooksUiState.Empty -> SavedBooksEmpty(modifier)
        is SavedBooksUiState.Error -> SavedBooksError(
            message = uiState.message,
            onRetry = onRetry,
            modifier = modifier
        )
        is SavedBooksUiState.Success -> SavedBooksContent(
            uiState = uiState,
            onBookClick = onBookClick,
            onRemoveBook = onRemoveBook,
            modifier = modifier
        )
    }
}

/**
 * Contenido principal cuando existen libros guardados.
 */
@Composable
private fun SavedBooksContent(
    uiState: SavedBooksUiState.Success,
    onBookClick: (String) -> Unit,
    onRemoveBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SavedBooksHeader(
                title = "Libros guardados",
                subtitle = "Tu lista tranquila para volver a lecturas con sentido cuando quieras."
            )
        }

        uiState.message?.let { message ->
            item {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        items(
            items = uiState.books,
            key = { book -> book.id }
        ) { book ->
            SavedBookCard(
                book = book,
                isRemoving = uiState.removingBookId == book.id,
                onBookClick = { onBookClick(book.id) },
                onRemoveBook = { onRemoveBook(book.id) }
            )
        }
    }
}

/**
 * Tarjeta individual de un libro guardado.
 *
 * Permite abrir el detalle tocando la tarjeta y eliminar el libro desde el icono
 * lateral sin abandonar la pantalla.
 */
@Composable
private fun SavedBookCard(
    book: SavedBook,
    isRemoving: Boolean,
    onBookClick: () -> Unit,
    onRemoveBook: () -> Unit
) {
    ElevatedCard(
        onClick = onBookClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.imageUrl,
                contentDescription = "Portada del libro",
                modifier = Modifier
                    .size(width = 72.dp, height = 104.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.tertiary),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.authors.joinToString().ifBlank { "Autor no disponible" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Toca para abrir el detalle",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onRemoveBook,
                enabled = !isRemoving
            ) {
                if (isRemoving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Quitar de guardados",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Cabecera de la biblioteca personal.
 */
@Composable
private fun SavedBooksHeader(
    title: String,
    subtitle: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Estado de carga inicial de la pantalla Guardados.
 */
@Composable
private fun SavedBooksLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text("Cargando tus libros guardados...")
        }
    }
}

/**
 * Estado mostrado cuando no hay sesion activa.
 */
@Composable
private fun SavedBooksLoginRequired(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SavedBooksCenteredCard(
        modifier = modifier,
        icon = Icons.Default.Lock,
        title = "Inicia sesion para guardar libros",
        message = "Tu biblioteca personal se sincroniza con tu cuenta para que puedas volver a esos libros cuando quieras.",
        action = {
            Button(onClick = onLoginClick) {
                Text("Ir a iniciar sesion")
            }
        }
    )
}

/**
 * Estado vacio para usuarios autenticados sin libros guardados.
 */
@Composable
private fun SavedBooksEmpty(modifier: Modifier = Modifier) {
    SavedBooksCenteredCard(
        modifier = modifier,
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = "Aun no tienes libros guardados",
        message = "Abre un libro desde Inicio y marca la lectura de hoy para construir tu lista tranquila.",
        action = null
    )
}

/**
 * Estado de error con accion de reintento.
 */
@Composable
private fun SavedBooksError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    SavedBooksCenteredCard(
        modifier = modifier,
        icon = Icons.Default.WarningAmber,
        title = "No pudimos cargar tus libros",
        message = message,
        action = {
            OutlinedButton(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    )
}

/**
 * Card centrada reutilizable para estados no-listado.
 */
@Composable
private fun SavedBooksCenteredCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    action: (@Composable () -> Unit)?
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = WarmAmber,
                    modifier = Modifier.size(34.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                action?.invoke()
            }
        }
    }
}
