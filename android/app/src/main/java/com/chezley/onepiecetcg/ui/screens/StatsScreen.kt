package com.chezley.onepiecetcg.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.repository.RoomCardRepository
import com.chezley.onepiecetcg.ui.theme.OnePieceTCGTheme

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: StatsViewModel = viewModel(factory = remember { statsViewModelFactory(context) })
    val state by viewModel.uiState.collectAsState()

    // This screen is removed from composition when the user switches tabs
    // (single NavHost, no per-tab back stack), so re-entering it re-runs
    // this effect -- that is what makes the figures reflect owned-card
    // changes made from other screens without an app relaunch.
    LaunchedEffect(Unit) { viewModel.refresh() }

    StatsContent(state = state, modifier = modifier)
}

@Composable
private fun StatsContent(state: StatsUiState?, modifier: Modifier = Modifier) {
    when {
        state == null -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        state.isEmpty -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Nothing owned yet — mark some cards owned to see your stats here.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Total cards owned: ${state.totalCopiesOwned}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Unique cards owned: ${state.totalUniqueCardsOwned}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            items(state.setCompletions, key = { it.setCode }) { completion ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "${completion.setCode}: ${completion.ownedUniqueCount}/${completion.totalCount}" +
                            " (${completion.percentage.toInt()}%)",
                    )
                    LinearProgressIndicator(
                        progress = { completion.percentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

private fun statsViewModelFactory(context: Context) = viewModelFactory {
    initializer {
        StatsViewModel(RoomCardRepository(AppDatabase.getInstance(context)))
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenEmptyPreview() {
    OnePieceTCGTheme {
        StatsContent(state = StatsUiState(totalCopiesOwned = 0, totalUniqueCardsOwned = 0, setCompletions = emptyList()))
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenContentPreview() {
    OnePieceTCGTheme {
        StatsContent(
            state = StatsUiState(
                totalCopiesOwned = 12,
                totalUniqueCardsOwned = 9,
                setCompletions = listOf(SetCompletion(setCode = "OP01", ownedUniqueCount = 9, totalCount = 121)),
            ),
        )
    }
}
