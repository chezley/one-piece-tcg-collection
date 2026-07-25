package com.chezley.onepiecetcg.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

enum class OnePieceDestination(val route: String, val label: String, val icon: ImageVector) {
    Browse(route = "browse", label = "Browse", icon = Icons.Filled.GridView),
    Collection(route = "collection", label = "Collection", icon = Icons.Filled.Bookmarks),
    Stats(route = "stats", label = "Stats", icon = Icons.Filled.ShowChart),
    Settings(route = "settings", label = "Settings", icon = Icons.Filled.Settings)
}
