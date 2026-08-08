package com.chezley.onepiecetcg.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.chezley.onepiecetcg.ui.theme.OnePieceTCGTheme

/**
 * One row in the Settings list. [subtitle] is static supporting text (e.g. the
 * app version); [onClick] is left null for purely informational rows. Future
 * prefs (price-tracking currency, theme, ...) are just another entry here —
 * the screen doesn't need to be restructured to add one.
 */
private data class SettingsItem(
    val title: String,
    val subtitle: String? = null,
    val onClick: (() -> Unit)? = null
)

private fun currentAppVersionName(context: Context): String =
    try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    } catch (e: PackageManager.NameNotFoundException) {
        "unknown"
    }

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val versionName = remember { currentAppVersionName(context) }

    var showAbout by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }

    val settingsItems = listOf(
        SettingsItem(title = "Version", subtitle = versionName),
        SettingsItem(title = "About", onClick = { showAbout = true }),
        SettingsItem(title = "Open Source Licenses", onClick = { showLicenses = true })
    )

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(settingsItems) { item ->
            ListItem(
                headlineContent = { Text(item.title) },
                supportingContent = if (item.subtitle != null) {
                    { Text(item.subtitle) }
                } else {
                    null
                },
                modifier = if (item.onClick != null) {
                    Modifier.clickable(onClick = item.onClick)
                } else {
                    Modifier
                }
            )
            HorizontalDivider()
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("OK") }
            },
            title = { Text("About") },
            text = {
                Text("OnePieceTCG is an unofficial collection tracker for the One Piece Card Game.")
            }
        )
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            confirmButton = {
                TextButton(onClick = { showLicenses = false }) { Text("OK") }
            },
            title = { Text("Open Source Licenses") },
            text = {
                Text(
                    "This app is built with Jetpack Compose, Material 3, and Navigation " +
                        "Compose (Apache License 2.0), and Kotlin (Apache License 2.0)."
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    OnePieceTCGTheme {
        SettingsScreen()
    }
}
