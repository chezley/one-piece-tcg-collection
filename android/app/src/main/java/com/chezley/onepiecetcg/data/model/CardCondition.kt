package com.chezley.onepiecetcg.data.model

/** Physical condition of a physical card copy, used by [OwnedCard]. Mirrors iOS `CardCondition`. */
enum class CardCondition {
    NEAR_MINT,
    LIGHTLY_PLAYED,
    MODERATELY_PLAYED,
    HEAVILY_PLAYED,
    DAMAGED,
}
