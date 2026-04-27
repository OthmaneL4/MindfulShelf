package com.example.mindfulshelf.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import com.example.mindfulshelf.data.preferences.ThemePreferencesRepository
import com.example.mindfulshelf.domain.repository.AuthRepository
import com.example.mindfulshelf.domain.repository.BooksRepository
import com.example.mindfulshelf.domain.repository.ReadingChallengeRepository
import com.example.mindfulshelf.domain.usecase.GetBookDetailUseCase
import com.example.mindfulshelf.domain.usecase.GetBooksForPresetUseCase
import com.example.mindfulshelf.presentation.detail.DetailScreen
import com.example.mindfulshelf.presentation.detail.DetailViewModel
import com.example.mindfulshelf.presentation.detail.DetailViewModelFactory
import com.example.mindfulshelf.presentation.home.HomeScreen
import com.example.mindfulshelf.presentation.home.HomeViewModel
import com.example.mindfulshelf.presentation.home.HomeViewModelFactory
import com.example.mindfulshelf.presentation.login.LoginScreen
import com.example.mindfulshelf.presentation.login.LoginViewModel
import com.example.mindfulshelf.presentation.login.LoginViewModelFactory
import com.example.mindfulshelf.presentation.saved.SavedBooksScreen
import com.example.mindfulshelf.presentation.saved.SavedBooksViewModel
import com.example.mindfulshelf.presentation.saved.SavedBooksViewModelFactory
import com.example.mindfulshelf.presentation.settings.SettingsScreen
import com.example.mindfulshelf.presentation.settings.SettingsViewModel
import com.example.mindfulshelf.presentation.settings.SettingsViewModelFactory

/**
 * Grafo de navegacion principal de la app.
 *
 * Conecta Home y Detail, crea los ViewModels con sus dependencias y define como
 * se pasa el `bookId` entre pantallas. Esta clase mantiene la navegacion fuera
 * de las pantallas para que los composables sean mas reutilizables.
 */
@Composable
fun MindfulShelfNavHost(
    booksRepository: BooksRepository,
    authRepository: AuthRepository,
    readingChallengeRepository: ReadingChallengeRepository,
    themePreferencesRepository: ThemePreferencesRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val bottomBarItems = remember { bottomNavigationItems() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val shouldShowBottomBar = bottomBarItems.any { item -> item.route == currentRoute }
    val homeFactory = remember(booksRepository) {
        HomeViewModelFactory(
            getBooksForPresetUseCase = GetBooksForPresetUseCase(booksRepository)
        )
    }
    val settingsFactory = remember(themePreferencesRepository) {
        SettingsViewModelFactory(themePreferencesRepository)
    }
    val loginFactory = remember(authRepository) {
        LoginViewModelFactory(authRepository)
    }
    val savedBooksFactory = remember(authRepository, readingChallengeRepository) {
        SavedBooksViewModelFactory(
            authRepository = authRepository,
            readingChallengeRepository = readingChallengeRepository
        )
    }
    val navigateToTopLevelRoute: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (shouldShowBottomBar) {
                MindfulShelfBottomBar(
                    items = bottomBarItems,
                    currentRoute = currentRoute,
                    onItemClick = navigateToTopLevelRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MindfulShelfRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MindfulShelfRoute.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(factory = homeFactory)
                val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

                HomeScreen(
                    uiState = uiState,
                    onMoodSelected = homeViewModel::onMoodSelected,
                    onReadingLengthSelected = homeViewModel::onReadingLengthSelected,
                    onSearchClick = homeViewModel::searchBooks,
                    onRetry = homeViewModel::retrySearch,
                    onBookClick = { bookId ->
                        navController.navigate(MindfulShelfRoute.Detail.createRoute(bookId))
                    }
                )
            }

            composable(MindfulShelfRoute.SavedBooks.route) {
                val savedBooksViewModel: SavedBooksViewModel = viewModel(factory = savedBooksFactory)
                val uiState by savedBooksViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    savedBooksViewModel.loadSavedBooks()
                }

                SavedBooksScreen(
                    uiState = uiState,
                    onLoginClick = {
                        navController.navigate(
                            MindfulShelfRoute.LoginRedirect.createRoute(
                                MindfulShelfRoute.SavedBooks.route
                            )
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onRetry = savedBooksViewModel::loadSavedBooks,
                    onBookClick = { bookId ->
                        navController.navigate(MindfulShelfRoute.Detail.createRoute(bookId))
                    },
                    onRemoveBook = savedBooksViewModel::removeSavedBook
                )
            }

            composable(MindfulShelfRoute.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
                val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

                SettingsScreen(
                    uiState = uiState,
                    onBack = navController::popBackStack,
                    onPaletteSelected = settingsViewModel::onPaletteSelected,
                    onBackgroundSelected = settingsViewModel::onBackgroundSelected,
                    onCardStyleSelected = settingsViewModel::onCardStyleSelected,
                    onResetTheme = settingsViewModel::resetTheme
                )
            }

            composable(MindfulShelfRoute.Login.route) {
                val loginViewModel: LoginViewModel = viewModel(factory = loginFactory)
                val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

                LoginScreen(
                    uiState = uiState,
                    onModeSelected = loginViewModel::onModeSelected,
                    onDisplayNameChanged = loginViewModel::onDisplayNameChanged,
                    onEmailChanged = loginViewModel::onEmailChanged,
                    onPasswordChanged = loginViewModel::onPasswordChanged,
                    onSubmit = loginViewModel::submit,
                    onGoogleIdTokenReceived = loginViewModel::signInWithGoogle,
                    onGoogleSignInError = loginViewModel::showAuthMessage,
                    onSignOut = loginViewModel::signOut,
                    onGoHome = {
                        navigateToTopLevelRoute(MindfulShelfRoute.Home.route)
                    }
                )
            }

            composable(
                route = MindfulShelfRoute.LoginRedirect.route,
                arguments = listOf(
                    navArgument(MindfulShelfRoute.LoginRedirect.ARG_REDIRECT) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val loginViewModel: LoginViewModel = viewModel(factory = loginFactory)
                val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
                val redirectRoute = backStackEntry.arguments
                    ?.getString(MindfulShelfRoute.LoginRedirect.ARG_REDIRECT)
                    ?.takeIf { route -> route.isKnownTopLevelRoute() }
                    ?: MindfulShelfRoute.Home.route

                LaunchedEffect(uiState.authenticatedUser?.uid, redirectRoute) {
                    if (uiState.authenticatedUser != null) {
                        navController.navigate(redirectRoute) {
                            popUpTo(MindfulShelfRoute.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

                LoginScreen(
                    uiState = uiState,
                    onModeSelected = loginViewModel::onModeSelected,
                    onDisplayNameChanged = loginViewModel::onDisplayNameChanged,
                    onEmailChanged = loginViewModel::onEmailChanged,
                    onPasswordChanged = loginViewModel::onPasswordChanged,
                    onSubmit = loginViewModel::submit,
                    onGoogleIdTokenReceived = loginViewModel::signInWithGoogle,
                    onGoogleSignInError = loginViewModel::showAuthMessage,
                    onSignOut = loginViewModel::signOut,
                    onGoHome = {
                        navigateToTopLevelRoute(MindfulShelfRoute.Home.route)
                    }
                )
            }

            composable(
                route = MindfulShelfRoute.Detail.route,
                arguments = listOf(
                    navArgument(MindfulShelfRoute.Detail.ARG_BOOK_ID) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments
                    ?.getString(MindfulShelfRoute.Detail.ARG_BOOK_ID)
                    .orEmpty()
                val detailFactory = remember(bookId, booksRepository, authRepository, readingChallengeRepository) {
                    DetailViewModelFactory(
                        bookId = bookId,
                        getBookDetailUseCase = GetBookDetailUseCase(booksRepository),
                        authRepository = authRepository,
                        readingChallengeRepository = readingChallengeRepository
                    )
                }
                val detailViewModel: DetailViewModel = viewModel(factory = detailFactory)
                val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()

                DetailScreen(
                    uiState = uiState,
                    onBack = navController::popBackStack,
                    onRetry = detailViewModel::loadBook,
                    onMarkAsTodayReading = detailViewModel::markAsTodayReading,
                    onLoginRequired = {
                        navController.navigate(
                            MindfulShelfRoute.LoginRedirect.createRoute(
                                MindfulShelfRoute.Home.route
                            )
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

/**
 * Valida que una ruta recibida como parametro pertenece a las secciones raiz.
 *
 * Evita navegar a destinos arbitrarios desde el parametro `redirect` y mantiene
 * el flujo de login limitado a pantallas conocidas.
 */
private fun String.isKnownTopLevelRoute(): Boolean {
    return this == MindfulShelfRoute.Home.route ||
        this == MindfulShelfRoute.SavedBooks.route ||
        this == MindfulShelfRoute.Settings.route ||
        this == MindfulShelfRoute.Login.route
}

/**
 * Barra inferior fija para las secciones principales de la app.
 *
 * Se mantiene fuera de las pantallas para no duplicar codigo y para que el
 * usuario pueda saltar entre Inicio, Configuracion e Inicio de sesion como en
 * una app social moderna.
 */
@Composable
private fun MindfulShelfBottomBar(
    items: List<BottomNavigationItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onItemClick(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}

private data class BottomNavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * Define las pestañas visibles en la barra inferior.
 */
private fun bottomNavigationItems(): List<BottomNavigationItem> {
    return listOf(
        BottomNavigationItem(
            route = MindfulShelfRoute.Home.route,
            label = "Inicio",
            icon = Icons.Default.Home
        ),
        BottomNavigationItem(
            route = MindfulShelfRoute.SavedBooks.route,
            label = "Guardados",
            icon = Icons.Default.BookmarkBorder
        ),
        BottomNavigationItem(
            route = MindfulShelfRoute.Settings.route,
            label = "Configuracion",
            icon = Icons.Default.Settings
        ),
        BottomNavigationItem(
            route = MindfulShelfRoute.Login.route,
            label = "Iniciar sesion",
            icon = Icons.Default.AccountCircle
        )
    )
}
