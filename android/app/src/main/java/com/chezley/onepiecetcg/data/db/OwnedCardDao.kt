package com.chezley.onepiecetcg.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface OwnedCardDao {
    @Insert
    suspend fun insert(ownedCard: OwnedCardEntity): Long

    @Update
    suspend fun update(ownedCard: OwnedCardEntity)

    @Delete
    suspend fun delete(ownedCard: OwnedCardEntity)

    @Query("SELECT * FROM owned_cards WHERE cardId = :cardId LIMIT 1")
    suspend fun getByCardId(cardId: String): OwnedCardEntity?

    @Transaction
    @Query("SELECT * FROM owned_cards ORDER BY dateAddedEpochMillis ASC")
    suspend fun getAllWithCard(): List<OwnedCardWithCard>
}
