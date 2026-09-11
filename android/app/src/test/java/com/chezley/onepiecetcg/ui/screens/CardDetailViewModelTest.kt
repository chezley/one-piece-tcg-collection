package com.chezley.onepiecetcg.ui.screens

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.db.toEntity
import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.repository.RoomCardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CardDetailViewModelTest {

    private lateinit var db: AppDatabase

    private val luffy = Card(
        id = "OP01-001",
        name = "Monkey D. Luffy",
        setCode = "OP01",
        cardNumber = "OP01-001",
        rarity = "L",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    private fun viewModel(): CardDetailViewModel =
        CardDetailViewModel(luffy.id, RoomCardRepository(db.cardDao(), db.ownedCardDao()))

    @Test
    fun loadsAnUnownedCardWithZeroQuantity() = runTest {
        db.cardDao().insertAll(listOf(luffy.toEntity()))

        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertEquals(luffy.id, state.card?.id)
        assertEquals(0, state.quantity)
        assertFalse(state.isOwned)
    }

    @Test
    fun loadsAnAlreadyOwnedCardWithItsQuantity() = runTest {
        db.cardDao().insertAll(listOf(luffy.toEntity()))
        RoomCardRepository(db.cardDao(), db.ownedCardDao()).addOwnedCard(luffy, quantity = 3)

        val state = viewModel().uiState.value

        assertEquals(3, state.quantity)
        assertTrue(state.isOwned)
    }

    @Test
    fun togglingOwnedOnAnUnownedCardSetsQuantityToOneAndPersists() = runTest {
        db.cardDao().insertAll(listOf(luffy.toEntity()))
        val vm = viewModel()

        vm.toggleOwned()

        assertEquals(1, vm.uiState.value.quantity)
        assertEquals(1, RoomCardRepository(db.cardDao(), db.ownedCardDao()).fetchOwnedCard(luffy.id)?.quantity)
    }

    @Test
    fun incrementingQuantitySetsTheExactValueRatherThanAdding() = runTest {
        db.cardDao().insertAll(listOf(luffy.toEntity()))
        val vm = viewModel()

        vm.incrementQuantity()
        vm.incrementQuantity()
        vm.incrementQuantity()

        assertEquals(3, vm.uiState.value.quantity)
        assertEquals(3, RoomCardRepository(db.cardDao(), db.ownedCardDao()).fetchOwnedCard(luffy.id)?.quantity)
    }

    @Test
    fun decrementingToZeroRemovesTheOwnedEntry() = runTest {
        db.cardDao().insertAll(listOf(luffy.toEntity()))
        val vm = viewModel()
        vm.setQuantity(1)

        vm.decrementQuantity()

        assertEquals(0, vm.uiState.value.quantity)
        assertFalse(vm.uiState.value.isOwned)
        assertNull(RoomCardRepository(db.cardDao(), db.ownedCardDao()).fetchOwnedCard(luffy.id))
    }

    @Test
    fun decrementingBelowZeroNeverGoesNegative() = runTest {
        db.cardDao().insertAll(listOf(luffy.toEntity()))
        val vm = viewModel()

        vm.decrementQuantity()

        assertEquals(0, vm.uiState.value.quantity)
    }

    @Test
    fun toggleOwnedOffRemovesTheOwnedEntry() = runTest {
        db.cardDao().insertAll(listOf(luffy.toEntity()))
        val vm = viewModel()
        vm.setQuantity(5)

        vm.toggleOwned()

        assertFalse(vm.uiState.value.isOwned)
        assertEquals(0, vm.uiState.value.quantity)
    }
}
