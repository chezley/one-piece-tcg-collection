package com.chezley.onepiecetcg.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val name: String,
    val setCode: String,
    val cardNumber: String,
    val rarity: String,
    val cost: Int?,
    val power: Int?,
    val attribute: String?,
    val type: String?,
    val imageUrl: String?,
)
