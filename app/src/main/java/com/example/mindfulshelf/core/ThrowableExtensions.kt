package com.example.mindfulshelf.core

/**
 * Convierte excepciones tecnicas en mensajes seguros para mostrar en UI.
 *
 * Se usa desde los ViewModels para evitar exponer trazas largas o detalles de
 * implementacion, manteniendo un mensaje claro cuando la API o la red fallan.
 */
fun Throwable.toUserMessage(fallbackMessage: String): String {
    return message
        ?.lineSequence()
        ?.firstOrNull()
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: fallbackMessage
}
