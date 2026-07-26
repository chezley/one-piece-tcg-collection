package com.chezley.onepiecetcg.data.repository

import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.CardCondition
import com.chezley.onepiecetcg.data.model.OwnedCard

/**
 * Repository API for cards and owned cards. UI code depends on this
 * interface, never on Room DAOs directly, so the persistence framework can
 * be swapped without touching screens.
 */
interface CardRepository {
    suspend fun fetchAllCards(): List<Card>

    suspend fun fetchCards(setCode: String): List<Card>

    suspend fun fetchOwnedCards(): List<OwnedCard>

    /** Adds [quantity] copies of [card] to the collection. [quantity] must be positive. */
    suspend fun addOwnedCard(
        card: Card,
        quantity: Int = 1,
        condition: CardCondition = CardCondition.NEAR_MINT,
    ): OwnedCard

    /** Sets [ownedCard]'s quantity to [quantity]; removes the entry if [quantity] is zero or negative. */
    suspend fun updateOwnedCard(ownedCard: OwnedCard, quantity: Int)

    suspend fun removeOwnedCard(ownedCard: OwnedCard)
}
