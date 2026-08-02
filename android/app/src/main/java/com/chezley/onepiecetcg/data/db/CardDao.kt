package com.chezley.onepiecetcg.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cards: List<CardEntity>): List<Long>

    @Query("SELECT * FROM cards ORDER BY name ASC")
    suspend fun getAll(): List<CardEntity>

    @Query("SELECT * FROM cards WHERE setCode = :setCode ORDER BY cardNumber ASC")
    suspend fun getBySet(setCode: String): List<CardEntity>
}
