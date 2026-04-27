package com.example.mindfulshelf.data.remote.dto

import com.example.mindfulshelf.domain.model.Book
import com.example.mindfulshelf.domain.model.BookDetail

/**
 * Respuesta cruda de Google Books para una busqueda.
 *
 * Los campos son nullable porque la API no garantiza que todos los libros
 * traigan portada, autores, descripcion o metadatos editoriales completos.
 */
data class VolumesResponseDto(
    val totalItems: Int? = null,
    val items: List<VolumeDto>? = null
)

/**
 * DTO principal de un volumen de Google Books.
 *
 * Representa la forma remota de los datos; no se usa directamente en UI para
 * evitar acoplar la presentacion al contrato externo de Google.
 */
data class VolumeDto(
    val id: String? = null,
    val volumeInfo: VolumeInfoDto? = null,
    val searchInfo: SearchInfoDto? = null
)

/**
 * Informacion bibliografica devuelta por la API para cada libro.
 */
data class VolumeInfoDto(
    val title: String? = null,
    val subtitle: String? = null,
    val authors: List<String>? = null,
    val description: String? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val pageCount: Int? = null,
    val categories: List<String>? = null,
    val previewLink: String? = null,
    val imageLinks: ImageLinksDto? = null
)

/**
 * Conjunto de URLs de portada en distintos tamanos.
 */
data class ImageLinksDto(
    val smallThumbnail: String? = null,
    val thumbnail: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null,
    val extraLarge: String? = null
)

/**
 * Fragmento destacado que Google Books puede devolver para algunas busquedas.
 */
data class SearchInfoDto(
    val textSnippet: String? = null
)

/**
 * Mapea un DTO remoto a un modelo de listado usado por la pantalla Home.
 *
 * Aqui se limpian textos HTML y se escoge la mejor portada disponible, de modo
 * que la UI reciba datos ya preparados y no tenga que conocer detalles de API.
 */
fun VolumeDto.toBook(recommendationReason: String): Book? {
    val info = volumeInfo ?: return null
    val resolvedId = id ?: return null

    return Book(
        id = resolvedId,
        title = info.title.orEmpty().ifBlank { "Lectura sin titulo" },
        authors = info.authors.orEmpty(),
        thumbnailUrl = info.imageLinks.bestImageUrl(),
        snippet = info.description.cleanText()
            ?: searchInfo?.textSnippet.cleanText()
            ?: info.subtitle.cleanText()
            ?: "Una recomendacion pensada para salir del scroll y volver a una lectura con mas calma.",
        recommendationReason = recommendationReason
    )
}

/**
 * Mapea un DTO remoto a un modelo de detalle usado por la pantalla Detail.
 */
fun VolumeDto.toBookDetail(): BookDetail? {
    val info = volumeInfo ?: return null
    val resolvedId = id ?: return null

    return BookDetail(
        id = resolvedId,
        title = info.title.orEmpty().ifBlank { "Lectura sin titulo" },
        authors = info.authors.orEmpty(),
        description = info.description.cleanText()
            ?: info.subtitle.cleanText()
            ?: "No hay sinopsis disponible para este libro todavia.",
        highResImageUrl = info.imageLinks.bestImageUrl(),
        categories = info.categories.orEmpty(),
        publisher = info.publisher.cleanText(),
        publishedDate = info.publishedDate.cleanText(),
        pageCount = info.pageCount,
        previewLink = info.previewLink.toHttps()
    )
}

/**
 * Selecciona la portada de mayor calidad disponible.
 *
 * Google Books puede devolver varias resoluciones y no siempre estan todas
 * presentes. Se prioriza la imagen grande para detalle, pero se acepta una
 * miniatura si es lo unico disponible.
 */
private fun ImageLinksDto?.bestImageUrl(): String? {
    return listOfNotNull(
        this?.extraLarge,
        this?.large,
        this?.medium,
        this?.small,
        this?.thumbnail,
        this?.smallThumbnail
    ).firstOrNull()?.toHttps()
}

/**
 * Normaliza texto recibido de la API antes de llevarlo a dominio.
 *
 * Algunas descripciones incluyen HTML o entidades escapadas. Limpiar aqui evita
 * que cada pantalla tenga que repetir esa misma proteccion.
 */
private fun String?.cleanText(): String? {
    return this
        ?.replace(Regex("<[^>]*>"), " ")
        ?.replace("&quot;", "\"")
        ?.replace("&#39;", "'")
        ?.replace("&amp;", "&")
        ?.replace(Regex("\\s+"), " ")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
}

/**
 * Fuerza HTTPS en enlaces externos cuando Google devuelve URLs antiguas.
 *
 * Esto mejora compatibilidad con politicas modernas de Android y evita abrir
 * recursos no seguros desde la app.
 */
private fun String?.toHttps(): String? {
    return this
        ?.replace("http://", "https://")
        ?.takeIf { it.isNotBlank() }
}
