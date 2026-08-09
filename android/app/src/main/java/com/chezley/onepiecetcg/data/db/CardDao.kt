package com.chezley.onepiecetcg.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY name")
    suspend fun fetchAll(): List<CardEntity>

    @Query("SELECT * FROM cards WHERE setCode = :setCode ORDER BY cardNumber")
    suspend fun fetchBySet(setCode: String): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun fetchById(id: String): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<CardEntity>)
}
