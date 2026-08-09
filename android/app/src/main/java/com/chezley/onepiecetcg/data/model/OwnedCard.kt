package com.chezley.onepiecetcg.data.model

/**
 * A card the user owns: a reference to a [Card] plus how many copies, in what
 * condition, and when it was added to the collection. Mirrors iOS `OwnedCard`.
 */
data class OwnedCard(
    val id: Long = 0,
    val card: Card,
    val quantity: Int,
    val condition: CardCondition = CardCondition.NEAR_MINT,
    /** Epoch millis. */
    val dateAdded: Long,
)
