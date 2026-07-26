package com.chezley.onepiecetcg.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.db.CardEntity
import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.repository.RoomCardRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomCardRepository

    private val zoro = Card(
        id = "OP01-001",
        name = "Roronoa Zoro",
        setCode = "OP01",
        cardNumber = "OP01-001",
        rarity = "L",
        cost = 5,
        power = 5000,
        attribute = "Slash",
        type = "Leader",
        imageUrl = "https://example.com/OP01-001.png",
    )

    private val law = Card(
        id = "OP01-002",
        name = "Trafalgar Law",
        setCode = "OP01",
        cardNumber = "OP01-002",
        rarity = "L",
    )

    private val luffy = Card(
        id = "OP02-001",
        name = "Monkey D. Luffy",
        setCode = "OP02",
        cardNumber = "OP02-001",
        rarity = "L",
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomCardRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun seedCards(vararg cards: Card) {
        database.cardDao().insertAll(cards.map { it.toEntity() })
    }

    private fun Card.toEntity() = CardEntity(
        id = id,
        name = name,
        setCode = setCode,
        cardNumber = cardNumber,
        rarity = rarity,
        cost = cost,
        power = power,
        attribute = attribute,
        type = type,
        imageUrl = imageUrl,
    )

    @Test
    fun `adding an owned card persists it and shows up in fetchOwnedCards`() = runTest {
        seedCards(zoro)

        val added = repository.addOwnedCard(zoro, quantity = 2)

        assertEquals(2, added.quantity)
        val owned = repository.fetchOwnedCards()
        assertEquals(1, owned.size)
        assertEquals(zoro.id, owned.first().card.id)
        assertEquals(2, owned.first().quantity)
    }

    @Test
    fun `adding the same card twice increments quantity instead of duplicating`() = runTest {
        seedCards(zoro)

        repository.addOwnedCard(zoro, quantity = 1)
        repository.addOwnedCard(zoro, quantity = 3)

        val owned = repository.fetchOwnedCards()
        assertEquals(1, owned.size)
        assertEquals(4, owned.first().quantity)
    }

    @Test
    fun `addOwnedCard rejects zero or negative quantity`() = runTest {
        seedCards(zoro)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.addOwnedCard(zoro, quantity = 0) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.addOwnedCard(zoro, quantity = -1) }
        }
        assertTrue(repository.fetchOwnedCards().isEmpty())
    }

    @Test
    fun `updateOwnedCard changes quantity`() = runTest {
        seedCards(zoro)
        val added = repository.addOwnedCard(zoro, quantity = 1)

        repository.updateOwnedCard(added, quantity = 5)

        val owned = repository.fetchOwnedCards()
        assertEquals(1, owned.size)
        assertEquals(5, owned.first().quantity)
    }

    @Test
    fun `updateOwnedCard to zero removes the owned card`() = runTest {
        seedCards(zoro)
        val added = repository.addOwnedCard(zoro, quantity = 2)

        repository.updateOwnedCard(added, quantity = 0)

        assertTrue(repository.fetchOwnedCards().isEmpty())
        assertNull(database.ownedCardDao().getByCardId(zoro.id))
    }

    @Test
    fun `removeOwnedCard deletes the entry`() = runTest {
        seedCards(zoro, law)
        val ownedZoro = repository.addOwnedCard(zoro, quantity = 1)
        repository.addOwnedCard(law, quantity = 1)

        repository.removeOwnedCard(ownedZoro)

        val owned = repository.fetchOwnedCards()
        assertEquals(1, owned.size)
        assertEquals(law.id, owned.first().card.id)
    }

    @Test
    fun `fetchCards filters by set`() = runTest {
        seedCards(zoro, law, luffy)

        val op01Cards = repository.fetchCards("OP01")

        assertEquals(2, op01Cards.size)
        assertTrue(op01Cards.all { it.setCode == "OP01" })
    }
}
