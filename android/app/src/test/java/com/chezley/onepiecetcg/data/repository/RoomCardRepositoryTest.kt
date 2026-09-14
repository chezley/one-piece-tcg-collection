package com.chezley.onepiecetcg.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.CardCondition
import com.chezley.onepiecetcg.data.db.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomCardRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CardRepository

    private val luffy = Card(
        id = "OP01-001",
        name = "Monkey D. Luffy",
        setCode = "OP01",
        cardNumber = "OP01-001",
        rarity = "L",
    )
    private val zoro = Card(
        id = "OP01-025",
        name = "Roronoa Zoro",
        setCode = "OP01",
        cardNumber = "OP01-025",
        rarity = "SR",
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomCardRepository(db.cardDao(), db.ownedCardDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addingAnOwnedCardPersistsItWithTheGivenQuantityAndCondition() = runBlocking {
        db.cardDao().insertAll(listOf(luffy.toEntity()))

        repository.addOwnedCard(luffy, quantity = 2, condition = CardCondition.LIGHTLY_PLAYED)

        val owned = repository.fetchOwnedCards()
        assertEquals(1, owned.size)
        assertEquals(luffy.id, owned.first().card.id)
        assertEquals(2, owned.first().quantity)
        assertEquals(CardCondition.LIGHTLY_PLAYED, owned.first().condition)
    }

    @Test
    fun addingTheSameCardAgainIncrementsTheExistingEntryInsteadOfDuplicating() = runBlocking {
        db.cardDao().insertAll(listOf(luffy.toEntity()))

        repository.addOwnedCard(luffy, quantity = 1)
        repository.addOwnedCard(luffy, quantity = 3)

        val owned = repository.fetchOwnedCards()
        assertEquals(1, owned.size)
        assertEquals(4, owned.first().quantity)
    }

    @Test
    fun concurrentAddOwnedCardCallsForTheSameCardNeverCreateDuplicateRows() = runBlocking {
        // Regression test for #47: addOwnedCard used to do a check-then-act
        // (read existing row, then insert/update) as two separate suspend
        // calls, so two callers racing for the same card could both read
        // "no existing row" and both insert, producing duplicate owned_cards
        // rows. Firing many concurrent calls on real background threads
        // exercises that race; the fix wraps the read+write in a single
        // Room @Transaction (OwnedCardDao.upsertQuantity), and SQLite
        // serializes transactions against one writer, so this must always
        // converge on exactly one row regardless of thread interleaving.
        db.cardDao().insertAll(listOf(luffy.toEntity()))
        val callCount = 20

        withContext(Dispatchers.IO) {
            (1..callCount).map {
                async { repository.addOwnedCard(luffy, quantity = 1) }
            }.awaitAll()
        }

        val owned = repository.fetchOwnedCards()
        assertEquals(1, owned.size)
        assertEquals(callCount, owned.first().quantity)
    }

    @Test
    fun addingAZeroQuantityThrowsAndCreatesNoEntry() = runBlocking {
        db.cardDao().insertAll(listOf(luffy.toEntity()))

        assertThrows(InvalidQuantityException::class.java) {
            runBlocking { repository.addOwnedCard(luffy, quantity = 0) }
        }

        assertTrue(repository.fetchOwnedCards().isEmpty())
    }

    @Test
    fun addingANegativeQuantityThrowsAndCreatesNoEntry() = runBlocking {
        db.cardDao().insertAll(listOf(luffy.toEntity()))

        assertThrows(InvalidQuantityException::class.java) {
            runBlocking { repository.addOwnedCard(luffy, quantity = -1) }
        }

        assertTrue(repository.fetchOwnedCards().isEmpty())
    }

    @Test
    fun updatingAnOwnedCardChangesItsQuantity() = runBlocking {
        db.cardDao().insertAll(listOf(luffy.toEntity()))
        val owned = repository.addOwnedCard(luffy, quantity = 1)

        repository.updateOwnedCard(owned, quantity = 5)

        assertEquals(5, repository.fetchOwnedCards().first().quantity)
    }

    @Test
    fun updatingAnOwnedCardToZeroQuantityRemovesIt() = runBlocking {
        db.cardDao().insertAll(listOf(luffy.toEntity()))
        val owned = repository.addOwnedCard(luffy, quantity = 1)

        repository.updateOwnedCard(owned, quantity = 0)

        assertTrue(repository.fetchOwnedCards().isEmpty())
    }

    @Test
    fun removingAnOwnedCardDeletesIt() = runBlocking {
        db.cardDao().insertAll(listOf(luffy.toEntity()))
        val owned = repository.addOwnedCard(luffy, quantity = 1)

        repository.removeOwnedCard(owned)

        assertTrue(repository.fetchOwnedCards().isEmpty())
        assertNull(db.ownedCardDao().getById(owned.id))
    }

    @Test
    fun fetchCardsBySetOnlyReturnsCardsInThatSet() = runBlocking {
        val otherSetCard = luffy.copy(id = "OP02-001", setCode = "OP02", cardNumber = "OP02-001")
        db.cardDao().insertAll(listOf(luffy.toEntity(), zoro.toEntity(), otherSetCard.toEntity()))

        val op01Cards = repository.fetchCards("OP01")

        assertEquals(setOf(luffy.id, zoro.id), op01Cards.map { it.id }.toSet())
    }

    @Test
    fun dataPersistsAcrossAppRelaunchAgainstTheSameUnderlyingDatabase() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fileDb = Room.databaseBuilder(context, AppDatabase::class.java, "relaunch-test.db")
            .allowMainThreadQueries()
            .build()
        try {
            val fileRepository = RoomCardRepository(fileDb.cardDao(), fileDb.ownedCardDao())
            fileDb.cardDao().insertAll(listOf(luffy.toEntity()))
            fileRepository.addOwnedCard(luffy, quantity = 3)
        } finally {
            fileDb.close()
        }

        // Simulate an app relaunch: reopen a fresh AppDatabase instance backed
        // by the same on-disk file rather than reusing the same in-memory object.
        val reopened = Room.databaseBuilder(context, AppDatabase::class.java, "relaunch-test.db")
            .allowMainThreadQueries()
            .build()
        try {
            val reopenedRepository = RoomCardRepository(reopened.cardDao(), reopened.ownedCardDao())
            val owned = reopenedRepository.fetchOwnedCards()
            assertEquals(1, owned.size)
            assertEquals(3, owned.first().quantity)
        } finally {
            reopened.close()
            context.deleteDatabase("relaunch-test.db")
        }
    }
}
