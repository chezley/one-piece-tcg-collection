package com.chezley.onepiecetcg.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.db.CardEntity
import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.CardCondition
import com.chezley.onepiecetcg.data.repository.InvalidQuantityException
import com.chezley.onepiecetcg.data.repository.RoomCardRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomCardRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: RoomCardRepository

    private fun context(): Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomCardRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun makeCard(id: String = "OP01-001", setCode: String = "OP01"): Card =
        Card(id = id, name = "Monkey D. Luffy", setCode = setCode, cardNumber = id, rarity = "L")

    private suspend fun seedCard(card: Card) {
        db.cardDao().insert(
            CardEntity(
                id = card.id,
                name = card.name,
                setCode = card.setCode,
                cardNumber = card.cardNumber,
                rarity = card.rarity,
                cost = card.cost,
                power = card.power,
                attribute = card.attribute,
                type = card.type,
                imageUrl = card.imageUrl,
            ),
        )
    }

    @Test
    fun addingOwnedCardCreatesEntry() = runTest {
        val card = makeCard()
        seedCard(card)

        repository.addOwnedCard(card, quantity = 2, condition = CardCondition.NEAR_MINT)

        val owned = repository.fetchOwnedCards()
        assertEquals(1, owned.size)
        assertEquals(2, owned.first().quantity)
        assertEquals(card.id, owned.first().card.id)
    }

    @Test
    fun addingOwnedCardTwiceIncrementsQuantityInsteadOfDuplicating() = runTest {
        val card = makeCard()
        seedCard(card)

        repository.addOwnedCard(card, quantity = 1)
        repository.addOwnedCard(card, quantity = 2)

        val owned = repository.fetchOwnedCards()
        assertEquals(1, owned.size)
        assertEquals(3, owned.first().quantity)
    }

    @Test
    fun addingOwnedCardWithZeroOrNegativeQuantityThrowsAndCreatesNoEntry() = runTest {
        val card = makeCard()
        seedCard(card)

        assertTrue(
            "expected InvalidQuantityException for quantity 0",
            runCatching { repository.addOwnedCard(card, quantity = 0) }.exceptionOrNull() is InvalidQuantityException,
        )
        assertTrue(
            "expected InvalidQuantityException for negative quantity",
            runCatching { repository.addOwnedCard(card, quantity = -1) }.exceptionOrNull() is InvalidQuantityException,
        )
        assertTrue(repository.fetchOwnedCards().isEmpty())
    }

    @Test
    fun removingOwnedCard() = runTest {
        val card = makeCard()
        seedCard(card)
        val ownedCard = repository.addOwnedCard(card, quantity = 1)

        repository.removeOwnedCard(ownedCard)

        assertTrue(repository.fetchOwnedCards().isEmpty())
    }

    @Test
    fun updatingQuantityToZeroRemovesTheOwnedCard() = runTest {
        val card = makeCard()
        seedCard(card)
        val ownedCard = repository.addOwnedCard(card, quantity = 3)

        repository.updateOwnedCard(ownedCard, quantity = 0)

        assertTrue(repository.fetchOwnedCards().isEmpty())
    }

    @Test
    fun fetchCardsBySet() = runTest {
        val op01Card = makeCard(id = "OP01-001", setCode = "OP01")
        val op02Card = makeCard(id = "OP02-001", setCode = "OP02")
        seedCard(op01Card)
        seedCard(op02Card)

        val result = repository.fetchCards("OP01")

        assertEquals(listOf("OP01-001"), result.map { it.id })
    }

    @Test
    fun dataPersistsAcrossRelaunchUsingFileBackedStore() = runTest {
        val dbFile = File(context().cacheDir, "${UUID.randomUUID()}.db")

        try {
            val firstLaunchDb = Room.databaseBuilder(context(), AppDatabase::class.java, dbFile.absolutePath)
                .allowMainThreadQueries()
                .build()
            val firstLaunchRepository = RoomCardRepository(firstLaunchDb)
            val card = makeCard()
            firstLaunchDb.cardDao().insert(
                CardEntity(
                    id = card.id,
                    name = card.name,
                    setCode = card.setCode,
                    cardNumber = card.cardNumber,
                    rarity = card.rarity,
                    cost = card.cost,
                    power = card.power,
                    attribute = card.attribute,
                    type = card.type,
                    imageUrl = card.imageUrl,
                ),
            )
            firstLaunchRepository.addOwnedCard(card, quantity = 4, condition = CardCondition.LIGHTLY_PLAYED)
            firstLaunchDb.close()

            val secondLaunchDb = Room.databaseBuilder(context(), AppDatabase::class.java, dbFile.absolutePath)
                .allowMainThreadQueries()
                .build()
            val secondLaunchRepository = RoomCardRepository(secondLaunchDb)

            val owned = secondLaunchRepository.fetchOwnedCards()
            assertEquals(1, owned.size)
            assertEquals(4, owned.first().quantity)
            assertEquals(CardCondition.LIGHTLY_PLAYED, owned.first().condition)
            secondLaunchDb.close()
        } finally {
            dbFile.delete()
        }
    }

    @Test
    fun fetchOwnedCardsSkipsEntriesWhoseCardWasNotFound() = runTest {
        val owned = repository.fetchOwnedCards()
        assertEquals(0, owned.size)
        assertNull(db.cardDao().fetchById("does-not-exist"))
    }
}
