package com.chezley.onepiecetcg.data.model

/**
 * A card the user owns: a reference to a [Card] plus how many copies, in
 * what condition, and when it was added to the collection.
 */
data class OwnedCard(
    val id: Long,
    val card: Card,
    val quantity: Int,
    val condition: CardCondition,
    val dateAddedEpochMillis: Long,
)
