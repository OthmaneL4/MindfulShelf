package com.example.mindfulshelf.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mindfulshelf.domain.model.Book
import com.example.mindfulshelf.domain.model.BookSearchPreset
import com.example.mindfulshelf.domain.model.MoodOption
import com.example.mindfulshelf.domain.model.ReadingLength
import com.example.mindfulshelf.ui.theme.SkyBlue
import com.example.mindfulshelf.ui.theme.WarmAmber

/**
 * Pantalla principal de MindfulShelf.
 *
 * Renderiza una experiencia guiada: primero el usuario elige animo, despues
 * tiempo disponible y finalmente recibe libros desde Google Books. La pantalla
 * no realiza trabajo de red; comunica eventos al ViewModel y pinta el estado.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onMoodSelected: (MoodOption) -> Unit,
    onReadingLengthSelected: (ReadingLength) -> Unit,
    onSearchClick: () -> Unit,
    onRetry: () -> Unit,
    onBookClick: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { HeroSection() }
            item {
                SectionTitle(
                    title = "1. Elige tu animo",
                    subtitle = "MindfulShelf transforma como te sientes en una busqueda con sentido."
                )
            }
            item {
                MoodSelector(
                    selectedMood = uiState.selectedMood,
                    onMoodSelected = onMoodSelected
                )
            }
            item {
                SectionTitle(
                    title = "2. Marca tu tiempo",
                    subtitle = "Asi te recomendamos lecturas que caben de verdad en tu dia."
                )
            }
            item {
                ReadingLengthSelector(
                    selectedReadingLength = uiState.selectedReadingLength,
                    onReadingLengthSelected = onReadingLengthSelected
                )
            }
            item {
                Button(
                    onClick = onSearchClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.selectedMood != null && uiState.selectedReadingLength != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Buscar una lectura con calma",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            uiState.currentPreset?.let { preset ->
                item { RecommendationHeader(preset = preset) }
            }

            when (val booksUiState = uiState.booksUiState) {
                BooksUiState.Idle -> item { IdleStateCard() }
                BooksUiState.Loading -> item { LoadingStateCard() }
                is BooksUiState.Empty -> item {
                    EmptyStateCard(
                        message = booksUiState.message,
                        onRetry = onRetry
                    )
                }

                is BooksUiState.Error -> item {
                    ErrorStateCard(
                        message = booksUiState.message,
                        onRetry = onRetry
                    )
                }

                is BooksUiState.Success -> items(
                    items = booksUiState.books,
                    key = { book -> book.id }
                ) { book ->
                    BookRecommendationCard(
                        book = book,
                        onBookClick = onBookClick
                    )
                }
            }
    }
}

/**
 * Bloque superior que explica el valor real de la app frente al doomscrolling.
 */
@Composable
private fun HeroSection() {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.surface
        )
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("Una alternativa saludable al doomscrolling") },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Menos scroll. Mas lectura profunda.",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Selecciona tu animo y el tiempo real que tienes. Nosotros lo traducimos a recomendaciones de Google Books pensadas para devolverte foco, curiosidad y calma.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Titulo reutilizable para separar visualmente las decisiones del usuario.
 */
@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Carrusel horizontal con los estados de animo disponibles.
 */
@Composable
private fun MoodSelector(
    selectedMood: MoodOption?,
    onMoodSelected: (MoodOption) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(MoodOption.entries) { mood ->
            MoodCard(
                moodOption = mood,
                isSelected = selectedMood == mood,
                onClick = { onMoodSelected(mood) }
            )
        }
    }
}

/**
 * Tarjeta seleccionable para un estado de animo.
 */
@Composable
private fun MoodCard(
    moodOption: MoodOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .width(210.dp)
            .height(172.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface
                )
                .height(172.dp)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) WarmAmber.copy(alpha = 0.26f) else SkyBlue.copy(alpha = 0.18f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = moodOption.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = moodOption.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = moodOption.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Selector de tiempo disponible. Se usa FilterChip porque comunica claramente
 * que solo hay una opcion activa dentro de este grupo.
 */
@Composable
private fun ReadingLengthSelector(
    selectedReadingLength: ReadingLength?,
    onReadingLengthSelected: (ReadingLength) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(ReadingLength.entries) { readingLength ->
            FilterChip(
                selected = selectedReadingLength == readingLength,
                onClick = { onReadingLengthSelected(readingLength) },
                label = {
                    Text("${readingLength.label} - ${readingLength.description}")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    }
}

/**
 * Cabecera que resume la busqueda generada antes del listado de libros.
 */
@Composable
private fun RecommendationHeader(preset: BookSearchPreset) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("${preset.mood.displayName} - ${preset.readingLength.label}") },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.tertiary,
                    disabledLabelColor = MaterialTheme.colorScheme.onTertiary
                )
            )
            Text(
                text = preset.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = preset.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Estado inicial antes de que el usuario lance una busqueda.
 */
@Composable
private fun IdleStateCard() {
    StatusCard(
        icon = Icons.Default.AutoAwesome,
        title = "Tu siguiente lectura empieza aqui",
        message = "Elige tu animo y el tiempo disponible para convertir una pausa cualquiera en una lectura con mas profundidad."
    )
}

/**
 * Estado de carga mientras se consulta la API.
 */
@Composable
private fun LoadingStateCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "Buscando lecturas que encajen contigo...",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Estamos consultando Google Books para proponerte una alternativa real al scroll infinito.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Estado vacio cuando Google Books no devuelve resultados utiles.
 */
@Composable
private fun EmptyStateCard(
    message: String,
    onRetry: () -> Unit
) {
    StatusCard(
        icon = Icons.Outlined.SearchOff,
        title = "Sin resultados por ahora",
        message = message,
        action = {
            OutlinedButton(onClick = onRetry) {
                Text("Probar otra vez")
            }
        }
    )
}

/**
 * Estado de error para fallos de red, API key o servicio remoto.
 */
@Composable
private fun ErrorStateCard(
    message: String,
    onRetry: () -> Unit
) {
    StatusCard(
        icon = Icons.Outlined.CloudOff,
        title = "No pudimos cargar las recomendaciones",
        message = message,
        action = {
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    )
}

/**
 * Componente comun para estados informativos de Home.
 */
@Composable
private fun StatusCard(
    icon: ImageVector,
    title: String,
    message: String,
    action: @Composable (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
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
            action?.invoke()
        }
    }
}

/**
 * Tarjeta de libro para el listado de recomendaciones.
 *
 * Incluye portada, autores, resumen y motivo de recomendacion para reforzar que
 * la sugerencia responde al animo y tiempo seleccionados.
 */
@Composable
private fun BookRecommendationCard(
    book: Book,
    onBookClick: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBookClick(book.id) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = book.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(92.dp)
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.tertiary),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("Lectura recomendada") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = WarmAmber.copy(alpha = 0.18f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (book.authors.isNotEmpty()) {
                        book.authors.joinToString()
                    } else {
                        "Autor no disponible"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = book.snippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.recommendationReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Iconografia visual asociada a cada estado de animo.
 */
private fun MoodOption.icon(): ImageVector {
    return when (this) {
        MoodOption.CALM -> Icons.Default.SelfImprovement
        MoodOption.FOCUS -> Icons.Default.Psychology
        MoodOption.INSPIRATION -> Icons.Default.AutoAwesome
        MoodOption.ESCAPE -> Icons.Default.TravelExplore
    }
}
