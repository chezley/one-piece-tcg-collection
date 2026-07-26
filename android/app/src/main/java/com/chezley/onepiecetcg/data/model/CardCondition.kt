package com.chezley.onepiecetcg.data.model

/** Physical condition of an owned card copy, used by [OwnedCard]. */
enum class CardCondition(val displayName: String) {
    NEAR_MINT("Near Mint"),
    LIGHTLY_PLAYED("Lightly Played"),
    MODERATELY_PLAYED("Moderately Played"),
    HEAVILY_PLAYED("Heavily Played"),
    DAMAGED("Damaged"),
}
