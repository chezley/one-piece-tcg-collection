package com.chezley.onepiecetcg.data.repository

import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.db.CardEntity
import com.chezley.onepiecetcg.data.db.OwnedCardEntity
import com.chezley.onepiecetcg.data.db.OwnedCardWithCard
import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.CardCondition
import com.chezley.onepiecetcg.data.model.OwnedCard

class RoomCardRepository(private val database: AppDatabase) : CardRepository {

    override suspend fun fetchAllCards(): List<Card> =
        database.cardDao().getAllCards().map { it.toDomain() }

    override suspend fun fetchCards(setCode: String): List<Card> =
        database.cardDao().getCardsBySet(setCode).map { it.toDomain() }

    override suspend fun fetchOwnedCards(): List<OwnedCard> =
        database.ownedCardDao().getAllWithCard().map { it.toDomain() }

    override suspend fun addOwnedCard(card: Card, quantity: Int, condition: CardCondition): OwnedCard {
        require(quantity > 0) { "quantity must be positive, was $quantity" }

        val dao = database.ownedCardDao()
        val existing = dao.getByCardId(card.id)
        val entity = if (existing != null) {
            existing.copy(quantity = existing.quantity + quantity).also { dao.update(it) }
        } else {
            val newEntity = OwnedCardEntity(
                cardId = card.id,
                quantity = quantity,
                condition = condition,
                dateAddedEpochMillis = System.currentTimeMillis(),
            )
            val id = dao.insert(newEntity)
            newEntity.copy(id = id)
        }
        return OwnedCard(
            id = entity.id,
            card = card,
            quantity = entity.quantity,
            condition = entity.condition,
            dateAddedEpochMillis = entity.dateAddedEpochMillis,
        )
    }

    override suspend fun updateOwnedCard(ownedCard: OwnedCard, quantity: Int) {
        val dao = database.ownedCardDao()
        val entity = OwnedCardEntity(
            id = ownedCard.id,
            cardId = ownedCard.card.id,
            quantity = quantity,
            condition = ownedCard.condition,
            dateAddedEpochMillis = ownedCard.dateAddedEpochMillis,
        )
        if (quantity > 0) {
            dao.update(entity)
        } else {
            dao.delete(entity)
        }
    }

    override suspend fun removeOwnedCard(ownedCard: OwnedCard) {
        val entity = OwnedCardEntity(
            id = ownedCard.id,
            cardId = ownedCard.card.id,
            quantity = ownedCard.quantity,
            condition = ownedCard.condition,
            dateAddedEpochMillis = ownedCard.dateAddedEpochMillis,
        )
        database.ownedCardDao().delete(entity)
    }

    private fun CardEntity.toDomain() = Card(
        id = id,
        name = name,
        setCode = setCode,
        cardNumber = cardNumber,
        rarity = rarity,
        cost = cost,
        power = power,
        attribute = attribute,
        type = type,
        imageUrl = imageUrl,
    )

    private fun OwnedCardWithCard.toDomain() = OwnedCard(
        id = ownedCard.id,
        card = card.toDomain(),
        quantity = ownedCard.quantity,
        condition = ownedCard.condition,
        dateAddedEpochMillis = ownedCard.dateAddedEpochMillis,
    )
}
