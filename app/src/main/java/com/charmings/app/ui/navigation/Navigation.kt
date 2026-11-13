package com.charmings.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.charmings.app.ui.screens.CaughtScreen
import com.charmings.app.ui.screens.DashboardScreen
import com.charmings.app.ui.screens.FieldScreen
import com.charmings.app.ui.screens.ProfileScreen
import com.charmings.app.ui.theme.Blueberry
import com.charmings.app.ui.theme.Lilac
import com.charmings.app.ui.theme.Milk
import com.charmings.app.ui.theme.Plum
import com.charmings.app.ui.theme.PlumSoft
import com.charmings.app.ui.theme.White
import com.charmings.app.ui.viewmodel.MainViewModel

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Walk("dashboard", "Прогулянка", Icons.Rounded.Home),
    Album("caught", "Знайдені", Icons.Rounded.Favorite),
    Hiding("field", "Невловимі", Icons.Rounded.Search)
}

private const val PROFILE_ROUTE = "profile/{petId}?celebrate={celebrate}"
private fun profileRoute(petId: Int, celebrate: Boolean) = "profile/$petId?celebrate=$celebrate"

@Composable
fun MainNavigation(
    viewModel: MainViewModel,
    pendingPetId: Int?,
    onPendingPetShown: () -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Deep link from a "caught" notification — also works when the app is already open.
    LaunchedEffect(pendingPetId) {
        val id = pendingPetId ?: return@LaunchedEffect
        viewModel.markSeen(id)
        navController.navigate(profileRoute(id, celebrate = true)) { launchSingleTop = true }
        onPendingPetShown()
    }

    Scaffold(
        containerColor = Milk,
        bottomBar = {
            if (Tab.values().any { it.route == currentRoute }) {
                NavigationBar(containerColor = White, tonalElevation = 0.dp) {
                    Tab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Blueberry,
                                selectedTextColor = Plum,
                                indicatorColor = Lilac,
                                unselectedIconColor = PlumSoft,
                                unselectedTextColor = PlumSoft
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Walk.route,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable(Tab.Walk.route) {
                val state by viewModel.dashboardState.collectAsStateWithLifecycle()
                DashboardScreen(
                    state = state,
                    onNewCatchClick = { id ->
                        viewModel.markSeen(id)
                        navController.navigate(profileRoute(id, celebrate = true))
                    },
                    onStartTracking = onStartTracking,
                    onStopTracking = onStopTracking
                )
            }
            composable(Tab.Album.route) {
                val state by viewModel.collectionState.collectAsStateWithLifecycle()
                CaughtScreen(state) { id ->
                    val wasNew = id in state.newIds
                    if (wasNew) viewModel.markSeen(id)
                    navController.navigate(profileRoute(id, celebrate = wasNew))
                }
            }
            composable(Tab.Hiding.route) {
                val state by viewModel.collectionState.collectAsStateWithLifecycle()
                FieldScreen(state)
            }
            composable(
                route = PROFILE_ROUTE,
                arguments = listOf(
                    navArgument("petId") { type = NavType.IntType },
                    navArgument("celebrate") { type = NavType.BoolType; defaultValue = false }
                )
            ) { entry ->
                val petId = entry.arguments?.getInt("petId") ?: return@composable
                ProfileScreen(
                    pet = viewModel.getPetById(petId),
                    isNew = entry.arguments?.getBoolean("celebrate") ?: false,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
