package com.chezley.onepiecetcg.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface OwnedCardDao {
    @Query("SELECT * FROM owned_cards ORDER BY dateAdded")
    suspend fun fetchAll(): List<OwnedCardEntity>

    @Query("SELECT * FROM owned_cards WHERE cardId = :cardId LIMIT 1")
    suspend fun fetchByCardId(cardId: String): OwnedCardEntity?

    @Insert
    suspend fun insert(ownedCard: OwnedCardEntity): Long

    @Update
    suspend fun update(ownedCard: OwnedCardEntity)

    @Query("DELETE FROM owned_cards WHERE id = :id")
    suspend fun deleteById(id: Long)
}
