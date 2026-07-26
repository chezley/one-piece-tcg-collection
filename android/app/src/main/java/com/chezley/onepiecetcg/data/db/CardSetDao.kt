package com.chezley.onepiecetcg.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sets: List<CardSetEntity>)

    @Query("SELECT * FROM card_sets ORDER BY code ASC")
    suspend fun getAllSets(): List<CardSetEntity>
}
