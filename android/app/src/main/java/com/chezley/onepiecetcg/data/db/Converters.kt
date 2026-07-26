package com.chezley.onepiecetcg.data.db

import androidx.room.TypeConverter
import com.chezley.onepiecetcg.data.model.CardCondition

class Converters {
    @TypeConverter
    fun fromCardCondition(condition: CardCondition): String = condition.name

    @TypeConverter
    fun toCardCondition(value: String): CardCondition = CardCondition.valueOf(value)
}
