package com.chezley.onepiecetcg.data.repository

import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.db.OwnedCardEntity
import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.CardCondition
import com.chezley.onepiecetcg.data.model.OwnedCard

class RoomCardRepository(private val db: AppDatabase) : CardRepository {

    override suspend fun fetchAllCards(): List<Card> = db.cardDao().fetchAll().map { it.toDomain() }

    override suspend fun fetchCards(setCode: String): List<Card> =
        db.cardDao().fetchBySet(setCode).map { it.toDomain() }

    override suspend fun fetchOwnedCards(): List<OwnedCard> =
        db.ownedCardDao().fetchAll().mapNotNull { owned ->
            db.cardDao().fetchById(owned.cardId)?.let { owned.toDomain(it.toDomain()) }
        }

    override suspend fun addOwnedCard(card: Card, quantity: Int, condition: CardCondition): OwnedCard {
        if (quantity <= 0) {
            throw InvalidQuantityException("quantity must be positive, was $quantity")
        }

        val existing = db.ownedCardDao().fetchByCardId(card.id)
        if (existing != null) {
            val updated = existing.copy(quantity = existing.quantity + quantity)
            db.ownedCardDao().update(updated)
            return updated.toDomain(card)
        }

        val entity = OwnedCardEntity(
            cardId = card.id,
            quantity = quantity,
            condition = condition,
            dateAdded = System.currentTimeMillis(),
        )
        val id = db.ownedCardDao().insert(entity)
        return entity.copy(id = id).toDomain(card)
    }

    override suspend fun updateOwnedCard(ownedCard: OwnedCard, quantity: Int) {
        if (quantity <= 0) {
            removeOwnedCard(ownedCard)
            return
        }

        db.ownedCardDao().update(
            OwnedCardEntity(
                id = ownedCard.id,
                cardId = ownedCard.card.id,
                quantity = quantity,
                condition = ownedCard.condition,
                dateAdded = ownedCard.dateAdded,
            ),
        )
    }

    override suspend fun removeOwnedCard(ownedCard: OwnedCard) {
        db.ownedCardDao().deleteById(ownedCard.id)
    }
}
