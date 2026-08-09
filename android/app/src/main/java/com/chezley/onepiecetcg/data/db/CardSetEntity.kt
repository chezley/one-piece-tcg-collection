package com.chezley.onepiecetcg.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "card_sets")
data class CardSetEntity(
    @PrimaryKey val code: String,
    val name: String,
    val releaseDate: Long?,
)
