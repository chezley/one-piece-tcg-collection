package com.chezley.onepiecetcg.data.repository

import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.CardCondition
import com.chezley.onepiecetcg.data.model.OwnedCard

/**
 * Repository API for cards and owned cards. UI code depends on this
 * interface, never on Room DAOs directly, so the persistence framework can
 * be swapped without touching screens. Mirrors iOS `CardRepository`.
 */
interface CardRepository {
    suspend fun fetchAllCards(): List<Card>
    suspend fun fetchCards(setCode: String): List<Card>
    suspend fun fetchOwnedCards(): List<OwnedCard>

    /** @throws InvalidQuantityException if [quantity] is not positive. */
    suspend fun addOwnedCard(
        card: Card,
        quantity: Int = 1,
        condition: CardCondition = CardCondition.NEAR_MINT,
    ): OwnedCard

    /** Removes [ownedCard] if [quantity] drops to zero or below. */
    suspend fun updateOwnedCard(ownedCard: OwnedCard, quantity: Int)

    suspend fun removeOwnedCard(ownedCard: OwnedCard)
}
