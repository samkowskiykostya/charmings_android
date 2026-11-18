package com.charmings.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.charmings.app.ui.components.CozyBackground
import com.charmings.app.ui.components.Paw
import com.charmings.app.ui.components.cozyCard
import com.charmings.app.ui.screens.CaughtScreen
import com.charmings.app.ui.screens.DashboardScreen
import com.charmings.app.ui.screens.FieldScreen
import com.charmings.app.ui.screens.ProfileScreen
import com.charmings.app.ui.screens.SettingsScreen
import com.charmings.app.ui.theme.Apricot
import com.charmings.app.ui.theme.Cocoa
import com.charmings.app.ui.theme.CocoaSoft
import com.charmings.app.ui.viewmodel.MainViewModel

/** [icon] == null means the hand-drawn paw. */
private enum class Tab(val route: String, val label: String, val icon: ImageVector?) {
    Walk("dashboard", "Прогулянка", null),
    Album("caught", "Знайдені", Icons.Rounded.Favorite),
    Hiding("field", "Невловимі", Icons.Rounded.Search)
}

private const val SETTINGS_ROUTE = "settings"
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

    CozyBackground(Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (Tab.values().any { it.route == currentRoute }) {
                CozyTabBar(currentRoute) { tab ->
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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
                    onStopTracking = onStopTracking,
                    onOpenSettings = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } }
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
                FieldScreen(state) { id -> navController.navigate(profileRoute(id, celebrate = false)) }
            }
            composable(SETTINGS_ROUTE) {
                SettingsScreen(onBack = { navController.popBackStack() })
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
}

/** A floating paper pill; the selected tab grows an apricot bean with its label. */
@Composable
private fun CozyTabBar(currentRoute: String?, onSelect: (Tab) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .cozyCard(corner = 34.dp, elevation = 10.dp, stitch = false)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.values().forEach { tab ->
                CozyTab(tab, selected = currentRoute == tab.route) { onSelect(tab) }
            }
        }
    }
}

@Composable
private fun CozyTab(tab: Tab, selected: Boolean, onClick: () -> Unit) {
    val pop by animateFloatAsState(
        if (selected) 1f else 0f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "tabPop"
    )
    val tint = if (selected) Cocoa else CocoaSoft
    Row(
        Modifier
            .heightIn(min = 48.dp)
            .background(Apricot.copy(alpha = pop.coerceIn(0f, 1f)), CircleShape)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconMod = Modifier
            .size(24.dp)
            .graphicsLayer {
                val s = 1f + 0.15f * pop
                scaleX = s; scaleY = s
                rotationZ = -8f * pop
            }
        // When collapsed, the icon carries the tab's name for TalkBack.
        val label = if (selected) null else tab.label
        if (tab.icon == null) Paw(tint, iconMod.semantics { label?.let { contentDescription = it } }, angle = -12f)
        else Icon(tab.icon, contentDescription = label, tint = tint, modifier = iconMod)
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut()
        ) {
            Row {
                Spacer(Modifier.width(8.dp))
                Text(tab.label, style = MaterialTheme.typography.labelLarge, color = Cocoa, maxLines = 1)
            }
        }
    }
}
