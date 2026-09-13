package com.chezley.onepiecetcg.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.CardCondition
import com.chezley.onepiecetcg.data.db.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    fun concurrentAddOwnedCardCallsForTheSameCardNeverCreateDuplicateRows() = runBlocking {
        db.cardDao().insertAll(listOf(luffy.toEntity()))

        // Guards against a check-then-act race: overlapping calls for the same card must not
        // both observe "no existing row" and each insert their own OwnedCardEntity.
        val concurrentCalls = 20
        coroutineScope {
            repeat(concurrentCalls) {
                launch(Dispatchers.Default) {
                    repository.addOwnedCard(luffy, quantity = 1)
                }
            }
        }

        val owned = repository.fetchOwnedCards()
        assertEquals(1, owned.size)
        assertEquals(concurrentCalls, owned.first().quantity)
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
