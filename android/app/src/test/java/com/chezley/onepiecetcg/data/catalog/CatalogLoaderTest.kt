package com.chezley.onepiecetcg.data.catalog

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chezley.onepiecetcg.data.db.AppDatabase
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
class CatalogLoaderTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `discovery finds the bundled OP01 set file`() {
        val names = CatalogLoader.discoverDatasetResourceNames(context.assets)

        assertEquals(listOf("OP01"), names)
    }

    @Test
    fun `dataset parses without error`() {
        val dataset = CatalogLoader.loadDataset("OP01", context.assets)

        assertTrue(dataset.cards.isNotEmpty())
    }

    @Test
    fun `dataset card count matches source data`() {
        val dataset = CatalogLoader.loadDataset("OP01", context.assets)

        assertEquals("OP01", dataset.set.code)
        assertEquals(121, dataset.cards.size)
    }

    @Test
    fun `known cards have expected fields`() {
        val dataset = CatalogLoader.loadDataset("OP01", context.assets)
        val cardsById = dataset.cards.associateBy { it.id }

        val zoro = requireNotNull(cardsById["OP01-001"])
        assertEquals("Roronoa Zoro", zoro.name)
        assertEquals("L", zoro.rarity)
        assertEquals("Leader", zoro.type)
        assertEquals(5, zoro.cost)
        assertEquals(5000, zoro.power)

        val usopp = requireNotNull(cardsById["OP01-004"])
        assertEquals("Usopp", usopp.name)
        assertEquals("R", usopp.rarity)
        assertEquals("Character", usopp.type)
    }

    @Test
    fun `loading an unknown resource throws`() {
        assertThrows(CatalogLoaderException::class.java) {
            CatalogLoader.loadDataset("NoSuchSet", context.assets)
        }
    }

    @Test
    fun `parseDataset rejects malformed json`() {
        assertThrows(org.json.JSONException::class.java) {
            CatalogLoader.parseDataset("{ this is not valid json")
        }
    }

    @Test
    fun `parseDataset leaves optional fields null when absent`() {
        val json = """
            {
              "set": { "code": "TS1", "name": "Test Set" },
              "cards": [
                { "id": "TS1-001", "name": "Test Card", "cardNumber": "TS1-001", "rarity": "C" }
              ]
            }
        """.trimIndent()

        val dataset = CatalogLoader.parseDataset(json)

        val card = dataset.cards.single()
        assertEquals("TS1-001", card.id)
        assertNull(card.cost)
        assertNull(card.power)
        assertNull(card.attribute)
        assertNull(card.type)
        assertNull(card.imageUrl)
    }

    @Test
    fun `seeding a dataset inserts cards and its set`() = runTest {
        val insertedCount = CatalogLoader.seedDataset("OP01", context.assets, database)

        assertEquals(121, insertedCount)
        assertEquals(121, database.cardDao().getAll().size)
        assertEquals(listOf("OP01"), database.cardSetDao().getAll().map { it.code })
    }

    @Test
    fun `seeding the same dataset twice does not duplicate`() = runTest {
        CatalogLoader.seedDataset("OP01", context.assets, database)
        val secondRunInsertedCount = CatalogLoader.seedDataset("OP01", context.assets, database)

        assertEquals(0, secondRunInsertedCount)
        assertEquals(121, database.cardDao().getAll().size)
        assertEquals(1, database.cardSetDao().getAll().size)
    }

    @Test
    fun `seedCatalog discovers and seeds every bundled set`() = runTest {
        val insertedCount = CatalogLoader.seedCatalog(context, database)

        assertEquals(121, insertedCount)
        assertEquals(121, database.cardDao().getAll().size)
    }

    @Test
    fun `seedCatalog run twice does not duplicate and does not requery per file`() = runTest {
        CatalogLoader.seedCatalog(context, database)
        val secondRunInsertedCount = CatalogLoader.seedCatalog(context, database)

        assertEquals(0, secondRunInsertedCount)
        assertEquals(121, database.cardDao().getAll().size)
        assertEquals(1, database.cardSetDao().getAll().size)
    }
}
