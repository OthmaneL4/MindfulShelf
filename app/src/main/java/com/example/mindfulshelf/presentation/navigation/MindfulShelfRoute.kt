package com.example.mindfulshelf.presentation.navigation

import android.net.Uri

/**
 * Definicion centralizada de rutas de navegacion.
 *
 * Tener las rutas en un unico lugar evita strings duplicados y hace mas seguro
 * construir la ruta de detalle con el id del libro codificado.
 */
sealed class MindfulShelfRoute(val route: String) {
    data object Home : MindfulShelfRoute("home")

    data object SavedBooks : MindfulShelfRoute("saved_books")

    data object Settings : MindfulShelfRoute("settings")

    data object Login : MindfulShelfRoute("login")

    data object LoginRedirect : MindfulShelfRoute("login_redirect/{redirect}") {
        const val ARG_REDIRECT = "redirect"

        /**
         * Construye una ruta de login que recuerda a donde volver tras autenticar.
         */
        fun createRoute(redirectRoute: String): String {
            return "login_redirect/${Uri.encode(redirectRoute)}"
        }
    }

    data object Detail : MindfulShelfRoute("detail/{bookId}") {
        const val ARG_BOOK_ID = "bookId"

        /**
         * Construye la ruta de detalle escapando el id original de Google Books.
         */
        fun createRoute(bookId: String): String {
            return "detail/${Uri.encode(bookId)}"
        }
    }
}
