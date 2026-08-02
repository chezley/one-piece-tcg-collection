package com.chezley.onepiecetcg.data.model

/** A released card set, e.g. "OP-01 Romance Dawn". */
data class CardSet(
    val code: String,
    val name: String,
    val releaseDate: Long? = null,
)
