package com.chezley.onepiecetcg.navigation

/**
 * Route for [com.chezley.onepiecetcg.ui.screens.CardDetailScreen]. Not one
 * of the bottom-nav [OnePieceDestination]s — reached by navigating here with
 * a card id from Browse (#28) or Collection (#29).
 */
object CardDetailRoute {
    const val ARG_CARD_ID = "cardId"
    const val ROUTE = "cardDetail/{$ARG_CARD_ID}"

    fun path(cardId: String) = "cardDetail/$cardId"
}
