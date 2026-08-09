package com.chezley.onepiecetcg.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.chezley.onepiecetcg.data.model.CardCondition

@Entity(
    tableName = "owned_cards",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("cardId")],
)
data class OwnedCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: String,
    val quantity: Int,
    val condition: CardCondition,
    /** Epoch millis. */
    val dateAdded: Long,
)
