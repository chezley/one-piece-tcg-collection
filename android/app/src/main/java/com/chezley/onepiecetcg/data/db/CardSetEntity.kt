package com.chezley.onepiecetcg.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chezley.onepiecetcg.data.model.CardSet

@Entity(tableName = "card_sets")
data class CardSetEntity(
    @PrimaryKey val code: String,
    val name: String,
    val releaseDate: Long?,
)

fun CardSetEntity.toDomain(): CardSet = CardSet(code = code, name = name, releaseDate = releaseDate)

fun CardSet.toEntity(): CardSetEntity = CardSetEntity(code = code, name = name, releaseDate = releaseDate)
