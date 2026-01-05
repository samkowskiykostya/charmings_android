package com.charmings.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.charmings.app.ui.screens.*
import com.charmings.app.ui.theme.*
import com.charmings.app.ui.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Dashboard : Screen("dashboard", "Головна", Icons.Default.Home)
    object Caught : Screen("caught", "Знайдені", Icons.Default.Favorite)
    object Field : Screen("field", "Невловимі", Icons.Outlined.Help)
    object Profile : Screen("profile/{petId}?celebrate={celebrate}", "Профіль", null) {
        fun createRoute(petId: Int, celebrate: Boolean = false) = "profile/$petId?celebrate=$celebrate"
    }
}

val bottomNavItems = listOf(Screen.Dashboard, Screen.Caught, Screen.Field)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    viewModel: MainViewModel,
    initialPetId: Int? = null,
    initialCelebrate: Boolean = false,
    onStartTracking: () -> Unit = {},
    onStopTracking: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Handle initial navigation for notification deep link
    LaunchedEffect(initialPetId) {
        if (initialPetId != null) {
            navController.navigate(Screen.Profile.createRoute(initialPetId, initialCelebrate))
        }
    }
    
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    
    Scaffold(
        topBar = {
            if (showBottomBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                Screen.Dashboard.route -> "Монітор активності"
                                Screen.Caught.route -> "Знайдені чарівнятка"
                                Screen.Field.route -> "Загадкові чарівнятка"
                                else -> ""
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = LightPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = White
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = screen.title,
                                        tint = if (selected) Primary else DarkGray
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 12.sp,
                                    color = if (selected) Primary else DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = LightPrimary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                val state by viewModel.dashboardState.collectAsState()
                DashboardScreen(
                    state = state,
                    onNewCatchClick = { petId ->
                        viewModel.removeFromNewCatches(petId)
                        navController.navigate(Screen.Profile.createRoute(petId, true))
                    },
                    onStartTracking = onStartTracking,
                    onStopTracking = onStopTracking
                )
            }
            
            composable(Screen.Caught.route) {
                val state by viewModel.caughtPetsState.collectAsState()
                LaunchedEffect(Unit) {
                    viewModel.loadCaughtPets()
                }
                CaughtScreen(
                    state = state,
                    onPetClick = { petId ->
                        navController.navigate(Screen.Profile.createRoute(petId, false))
                    }
                )
            }
            
            composable(Screen.Field.route) {
                val state by viewModel.uncaughtPetsState.collectAsState()
                LaunchedEffect(Unit) {
                    viewModel.loadUncaughtPets()
                }
                FieldScreen(state = state)
            }
            
            composable(
                route = Screen.Profile.route,
                arguments = listOf(
                    navArgument("petId") { type = NavType.IntType },
                    navArgument("celebrate") { 
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getInt("petId") ?: return@composable
                val celebrate = backStackEntry.arguments?.getBoolean("celebrate") ?: false
                val pet = viewModel.getPetById(petId)
                
                ProfileScreen(
                    pet = pet,
                    isNew = celebrate,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
