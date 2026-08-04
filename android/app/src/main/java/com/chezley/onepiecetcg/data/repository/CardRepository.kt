package com.chezley.onepiecetcg.data.repository

import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.CardCondition
import com.chezley.onepiecetcg.data.model.OwnedCard

/** Thrown when an owned-card operation is given an invalid quantity. */
class InvalidQuantityException(val quantity: Int) :
    IllegalArgumentException("Quantity must be positive, got $quantity")

/**
 * Repository API for cards and owned cards. UI code depends on this
 * interface, never on Room DAOs directly, so the persistence framework can
 * be swapped without touching UI code. Mirrors `CardRepository` on iOS.
 */
interface CardRepository {
    suspend fun fetchAllCards(): List<Card>
    suspend fun fetchCards(setCode: String): List<Card>
    suspend fun fetchOwnedCards(): List<OwnedCard>

    /**
     * Adds [quantity] copies of [card] to the collection, or increments the
     * existing entry for that card if one is already owned.
     *
     * @throws InvalidQuantityException if [quantity] is not positive.
     */
    suspend fun addOwnedCard(card: Card, quantity: Int = 1, condition: CardCondition = CardCondition.NEAR_MINT): OwnedCard

    /**
     * Sets [ownedCard]'s quantity to [quantity]. A [quantity] of zero or
     * less removes the entry entirely, mirroring [removeOwnedCard].
     */
    suspend fun updateOwnedCard(ownedCard: OwnedCard, quantity: Int)

    suspend fun removeOwnedCard(ownedCard: OwnedCard)
}
