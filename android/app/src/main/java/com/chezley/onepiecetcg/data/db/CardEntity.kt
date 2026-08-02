package com.chezley.onepiecetcg.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chezley.onepiecetcg.data.model.Card

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

fun CardEntity.toDomain(): Card = Card(
    id = id,
    name = name,
    setCode = setCode,
    cardNumber = cardNumber,
    rarity = rarity,
    cost = cost,
    power = power,
    attribute = attribute,
    type = type,
    imageUrl = imageUrl,
)

fun Card.toEntity(): CardEntity = CardEntity(
    id = id,
    name = name,
    setCode = setCode,
    cardNumber = cardNumber,
    rarity = rarity,
    cost = cost,
    power = power,
    attribute = attribute,
    type = type,
    imageUrl = imageUrl,
)
