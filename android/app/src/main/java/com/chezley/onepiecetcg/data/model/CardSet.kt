package com.chezley.onepiecetcg.data.model

/** A released card set, e.g. "OP-01 Romance Dawn". Mirrors iOS `CardSet`. */
data class CardSet(
    val code: String,
    val name: String,
    /** Epoch millis, or null if unknown. */
    val releaseDate: Long? = null,
)
