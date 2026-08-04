package com.chezley.onepiecetcg.data.model

/** Physical condition of a physical card copy, used by [OwnedCard]. */
enum class CardCondition(val label: String) {
    NEAR_MINT("Near Mint"),
    LIGHTLY_PLAYED("Lightly Played"),
    MODERATELY_PLAYED("Moderately Played"),
    HEAVILY_PLAYED("Heavily Played"),
    DAMAGED("Damaged"),
}
