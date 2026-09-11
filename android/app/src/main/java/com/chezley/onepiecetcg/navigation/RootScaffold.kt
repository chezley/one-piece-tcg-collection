package com.chezley.onepiecetcg.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chezley.onepiecetcg.ui.screens.BrowseScreen
import com.chezley.onepiecetcg.ui.screens.CardDetailScreen
import com.chezley.onepiecetcg.ui.screens.CollectionScreen
import com.chezley.onepiecetcg.ui.screens.SettingsScreen
import com.chezley.onepiecetcg.ui.screens.StatsScreen

@Composable
fun RootScaffold() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                OnePieceDestination.entries.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = OnePieceDestination.Browse.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(OnePieceDestination.Browse.route) { BrowseScreen() }
            composable(OnePieceDestination.Collection.route) { CollectionScreen() }
            composable(OnePieceDestination.Stats.route) { StatsScreen() }
            composable(OnePieceDestination.Settings.route) { SettingsScreen() }
            composable(
                route = CardDetailRoute.ROUTE,
                arguments = listOf(navArgument(CardDetailRoute.ARG_CARD_ID) { type = NavType.StringType }),
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString(CardDetailRoute.ARG_CARD_ID) ?: return@composable
                CardDetailScreen(cardId = cardId, onBack = { navController.popBackStack() })
            }
        }
    }
}
