package com.chezley.onepiecetcg.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardSetDao {
    @Query("SELECT * FROM card_sets ORDER BY code")
    suspend fun fetchAll(): List<CardSetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(set: CardSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sets: List<CardSetEntity>)
}
