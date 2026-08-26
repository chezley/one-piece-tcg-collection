package com.chezley.onepiecetcg.ui.screens

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.db.CardEntity
import com.chezley.onepiecetcg.data.db.OwnedCardEntity
import com.chezley.onepiecetcg.data.model.CardCondition
import com.chezley.onepiecetcg.data.repository.RoomCardRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StatsViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var viewModel: StatsViewModel

    private fun context(): Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = StatsViewModel(RoomCardRepository(db))
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun seedCard(id: String, setCode: String) {
        db.cardDao().insert(
            CardEntity(
                id = id,
                name = id,
                setCode = setCode,
                cardNumber = id,
                rarity = "C",
                cost = null,
                power = null,
                attribute = null,
                type = null,
                imageUrl = null,
            ),
        )
    }

    private suspend fun seedOwned(cardId: String, quantity: Int) {
        db.ownedCardDao().insert(
            OwnedCardEntity(
                cardId = cardId,
                quantity = quantity,
                condition = CardCondition.NEAR_MINT,
                dateAdded = 0L,
            ),
        )
    }

    @Test
    fun refreshWithNoOwnedCardsProducesEmptyState() = runTest {
        seedCard("OP01-001", "OP01")

        viewModel.refresh()

        val state = viewModel.uiState.value
        requireNotNull(state)
        assertTrue(state.isEmpty)
        assertEquals(0, state.totalCopiesOwned)
        assertEquals(0, state.totalUniqueCardsOwned)
    }

    @Test
    fun refreshComputesTotalsAndPerSetCompletion() = runTest {
        seedCard("OP01-001", "OP01")
        seedCard("OP01-002", "OP01")
        seedCard("OP01-003", "OP01")
        seedCard("ST01-001", "ST01")
        seedOwned("OP01-001", quantity = 2)
        seedOwned("OP01-002", quantity = 1)

        viewModel.refresh()

        val state = viewModel.uiState.value
        requireNotNull(state)
        assertTrue(!state.isEmpty)
        assertEquals(3, state.totalCopiesOwned)
        assertEquals(2, state.totalUniqueCardsOwned)

        val op01 = state.setCompletions.first { it.setCode == "OP01" }
        assertEquals(2, op01.ownedUniqueCount)
        assertEquals(3, op01.totalCount)
        assertEquals(66, op01.percentage.toInt())

        val st01 = state.setCompletions.first { it.setCode == "ST01" }
        assertEquals(0, st01.ownedUniqueCount)
        assertEquals(1, st01.totalCount)
        assertEquals(0f, st01.percentage, 0.001f)
    }
}
