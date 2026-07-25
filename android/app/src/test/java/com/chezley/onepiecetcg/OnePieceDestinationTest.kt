package com.chezley.onepiecetcg

import com.chezley.onepiecetcg.navigation.OnePieceDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class OnePieceDestinationTest {
    @Test
    fun `has four tabs in the expected order`() {
        assertEquals(
            listOf("browse", "collection", "stats", "settings"),
            OnePieceDestination.entries.map { it.route }
        )
    }
}
