package com.chezley.onepiecetcg.data.repository

import com.chezley.onepiecetcg.data.db.CardDao
import com.chezley.onepiecetcg.data.db.OwnedCardDao
import com.chezley.onepiecetcg.data.db.OwnedCardEntity
import com.chezley.onepiecetcg.data.db.toDomain
import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.CardCondition
import com.chezley.onepiecetcg.data.model.OwnedCard

class RoomCardRepository(
    private val cardDao: CardDao,
    private val ownedCardDao: OwnedCardDao,
) : CardRepository {

    override suspend fun fetchAllCards(): List<Card> = cardDao.getAll().map { it.toDomain() }

    override suspend fun fetchCards(setCode: String): List<Card> =
        cardDao.getBySet(setCode).map { it.toDomain() }

    override suspend fun fetchCard(id: String): Card? = cardDao.getById(id)?.toDomain()

    override suspend fun fetchOwnedCards(): List<OwnedCard> = ownedCardDao.getAll().map { it.toDomain() }

    override suspend fun fetchOwnedCard(cardId: String): OwnedCard? = ownedCardDao.getByCardId(cardId)?.toDomain()

    override suspend fun addOwnedCard(card: Card, quantity: Int, condition: CardCondition): OwnedCard {
        if (quantity <= 0) throw InvalidQuantityException(quantity)

        // Matched on cardId AND condition so a card owned in multiple
        // conditions gets a separate row per condition (see #46/#38).
        val existing = ownedCardDao.getByCardIdAndCondition(card.id, condition)
        if (existing != null) {
            val updated = existing.ownedCard.copy(quantity = existing.ownedCard.quantity + quantity)
            ownedCardDao.update(updated)
            return updated.toDomainWith(card)
        }

        val entity = OwnedCardEntity(
            cardId = card.id,
            quantity = quantity,
            condition = condition,
            dateAdded = System.currentTimeMillis(),
        )
        val id = ownedCardDao.insert(entity)
        return entity.copy(id = id).toDomainWith(card)
    }

    override suspend fun updateOwnedCard(ownedCard: OwnedCard, quantity: Int) {
        if (quantity <= 0) {
            removeOwnedCard(ownedCard)
            return
        }
        val entity = OwnedCardEntity(
            id = ownedCard.id,
            cardId = ownedCard.card.id,
            quantity = quantity,
            condition = ownedCard.condition,
            dateAdded = ownedCard.dateAdded,
        )
        ownedCardDao.update(entity)
    }

    override suspend fun removeOwnedCard(ownedCard: OwnedCard) {
        val entity = OwnedCardEntity(
            id = ownedCard.id,
            cardId = ownedCard.card.id,
            quantity = ownedCard.quantity,
            condition = ownedCard.condition,
            dateAdded = ownedCard.dateAdded,
        )
        ownedCardDao.delete(entity)
    }

    private fun OwnedCardEntity.toDomainWith(card: Card): OwnedCard = OwnedCard(
        id = id,
        card = card,
        quantity = quantity,
        condition = condition,
        dateAdded = dateAdded,
    )
}
