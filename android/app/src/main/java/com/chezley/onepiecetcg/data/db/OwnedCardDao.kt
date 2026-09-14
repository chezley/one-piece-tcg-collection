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
     * Atomically adds [quantityDelta] to the existing owned-card row for
     * [cardId], or inserts a new row if none exists yet.
     *
     * The read (getByCardId) and write (update/insert) below execute inside
     * one Room-managed database transaction because this method is
     * @Transaction-annotated: Room serializes all such transactions onto a
     * single writer, so two concurrent calls for the same cardId can no
     * longer both observe "no existing row" and both insert a duplicate
     * (see #47) — the second call always sees the first call's write.
     */
    @Transaction
    suspend fun upsertQuantity(cardId: String, quantityDelta: Int, condition: CardCondition, dateAdded: Long): OwnedCardEntity {
        val existing = getByCardId(cardId)
        if (existing != null) {
            val updated = existing.ownedCard.copy(quantity = existing.ownedCard.quantity + quantityDelta)
            update(updated)
            return updated
        }

        val entity = OwnedCardEntity(
            cardId = cardId,
            quantity = quantityDelta,
            condition = condition,
            dateAdded = dateAdded,
        )
        val id = insert(entity)
        return entity.copy(id = id)
    }
}
