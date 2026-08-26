package com.chezley.onepiecetcg.ui.screens

import androidx.lifecycle.ViewModel
import com.chezley.onepiecetcg.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Ownership progress for one set: how many of its unique cards the user owns. */
data class SetCompletion(
    val setCode: String,
    val ownedUniqueCount: Int,
    val totalCount: Int,
) {
    val percentage: Float
        get() = if (totalCount == 0) 0f else ownedUniqueCount.toFloat() / totalCount * 100f
}

data class StatsUiState(
    val totalCopiesOwned: Int,
    val totalUniqueCardsOwned: Int,
    val setCompletions: List<SetCompletion>,
) {
    val isEmpty: Boolean get() = totalUniqueCardsOwned == 0
}

/**
 * Loads collection-wide stats from [repository]. Holds no subscription of
 * its own to the underlying store -- [refresh] is called by the screen each
 * time it becomes visible (from a `LaunchedEffect`, which owns the
 * coroutine), which is how figures stay current after cards are
 * added/removed elsewhere without needing an app relaunch.
 */
class StatsViewModel(private val repository: CardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState?>(null)
    val uiState: StateFlow<StatsUiState?> = _uiState.asStateFlow()

    suspend fun refresh() {
        _uiState.value = loadStats()
    }

    private suspend fun loadStats(): StatsUiState {
        val owned = repository.fetchOwnedCards()
        val allCards = repository.fetchAllCards()

        val ownedCardIdsBySet: Map<String, Set<String>> = owned
            .groupBy { it.card.setCode }
            .mapValues { (_, cards) -> cards.map { it.card.id }.toSet() }

        val setCompletions = allCards
            .groupBy { it.setCode }
            .map { (setCode, cardsInSet) ->
                SetCompletion(
                    setCode = setCode,
                    ownedUniqueCount = ownedCardIdsBySet[setCode]?.size ?: 0,
                    totalCount = cardsInSet.distinctBy { it.id }.size,
                )
            }
            .sortedBy { it.setCode }

        return StatsUiState(
            totalCopiesOwned = owned.sumOf { it.quantity },
            totalUniqueCardsOwned = owned.distinctBy { it.card.id }.size,
            setCompletions = setCompletions,
        )
    }
}
