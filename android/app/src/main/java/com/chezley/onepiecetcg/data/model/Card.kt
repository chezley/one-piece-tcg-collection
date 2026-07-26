package com.chezley.onepiecetcg.data.model

/** A single card in the catalog, e.g. "Monkey D. Luffy" (OP01-003). */
data class Card(
    val id: String,
    val name: String,
    val setCode: String,
    val cardNumber: String,
    val rarity: String,
    val cost: Int? = null,
    val power: Int? = null,
    val attribute: String? = null,
    val type: String? = null,
    val imageUrl: String? = null,
)
