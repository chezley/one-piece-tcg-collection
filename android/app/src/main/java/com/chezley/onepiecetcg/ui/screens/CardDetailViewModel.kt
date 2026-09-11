package com.chezley.onepiecetcg.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.OwnedCard
import com.chezley.onepiecetcg.data.repository.CardRepository
import com.chezley.onepiecetcg.data.repository.RoomCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for [CardDetailScreen]. [ownedCard] is null when the card isn't owned. */
data class CardDetailUiState(
    val isLoading: Boolean = true,
    val card: Card? = null,
    val ownedCard: OwnedCard? = null,
) {
    val quantity: Int get() = ownedCard?.quantity ?: 0
    val isOwned: Boolean get() = quantity > 0
}

/**
 * Loads a single [Card] plus its owned-card entry (if any) and lets the
 * detail screen mark it owned / adjust quantity, persisting every change
 * through [CardRepository] immediately.
 */
class CardDetailViewModel(
    private val cardId: String,
    private val repository: CardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val card = repository.fetchCard(cardId)
            val ownedCard = repository.fetchOwnedCard(cardId)
            _uiState.value = CardDetailUiState(isLoading = false, card = card, ownedCard = ownedCard)
        }
    }

    /**
     * Sets the owned quantity to exactly [newQuantity]. Zero or less removes
     * the owned-card entry; going from zero creates it.
     */
    fun setQuantity(newQuantity: Int) {
        val state = _uiState.value
        val card = state.card ?: return
        val targetQuantity = newQuantity.coerceAtLeast(0)

        viewModelScope.launch {
            when {
                targetQuantity <= 0 && state.ownedCard != null -> {
                    repository.removeOwnedCard(state.ownedCard)
                    _uiState.value = state.copy(ownedCard = null)
                }
                targetQuantity <= 0 -> Unit
                state.ownedCard == null -> {
                    val created = repository.addOwnedCard(card, quantity = targetQuantity)
                    _uiState.value = state.copy(ownedCard = created)
                }
                else -> {
                    repository.updateOwnedCard(state.ownedCard, quantity = targetQuantity)
                    _uiState.value = state.copy(ownedCard = state.ownedCard.copy(quantity = targetQuantity))
                }
            }
        }
    }

    fun incrementQuantity() = setQuantity(_uiState.value.quantity + 1)

    fun decrementQuantity() = setQuantity(_uiState.value.quantity - 1)

    /** Toggles owned off (quantity -> 0) or on (quantity -> 1) if not yet owned. */
    fun toggleOwned() = setQuantity(if (_uiState.value.isOwned) 0 else 1)

    companion object {
        fun factory(cardId: String, context: Context) = viewModelFactory {
            initializer {
                val database = AppDatabase.getInstance(context.applicationContext)
                CardDetailViewModel(cardId, RoomCardRepository(database.cardDao(), database.ownedCardDao()))
            }
        }
    }
}
