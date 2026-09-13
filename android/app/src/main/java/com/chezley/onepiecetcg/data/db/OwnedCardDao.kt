package com.chezley.onepiecetcg.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.chezley.onepiecetcg.data.model.CardCondition

@Dao
interface OwnedCardDao {
    @Insert
    suspend fun insert(ownedCard: OwnedCardEntity): Long

    @Update
    suspend fun update(ownedCard: OwnedCardEntity)

    @Delete
    suspend fun delete(ownedCard: OwnedCardEntity)

    @Transaction
    @Query("SELECT * FROM owned_cards ORDER BY dateAdded ASC")
    suspend fun getAll(): List<OwnedCardWithCard>

    @Transaction
    @Query("SELECT * FROM owned_cards WHERE cardId = :cardId LIMIT 1")
    suspend fun getByCardId(cardId: String): OwnedCardWithCard?

    @Query("SELECT * FROM owned_cards WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): OwnedCardEntity?

    /**
     * Atomically adds [quantity] to the existing owned-card row for [cardId] (or creates one).
     *
     * Room runs this default method as a single transaction, so the read-then-write below can't
     * interleave with another concurrent call to this method for the same [cardId] and produce
     * duplicate rows.
     */
    @Transaction
    suspend fun upsertOwnedCard(cardId: String, quantity: Int, condition: CardCondition, dateAdded: Long): OwnedCardEntity {
        val existing = getByCardId(cardId)?.ownedCard
        if (existing != null) {
            val updated = existing.copy(quantity = existing.quantity + quantity)
            update(updated)
            return updated
        }

        val entity = OwnedCardEntity(cardId = cardId, quantity = quantity, condition = condition, dateAdded = dateAdded)
        val id = insert(entity)
        return entity.copy(id = id)
    }
}
