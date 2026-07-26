package com.chezley.onepiecetcg.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<CardEntity>)

    @Query("SELECT * FROM cards ORDER BY name ASC")
    suspend fun getAllCards(): List<CardEntity>

    @Query("SELECT * FROM cards WHERE setCode = :setCode ORDER BY cardNumber ASC")
    suspend fun getCardsBySet(setCode: String): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: String): CardEntity?
}
