package com.chezley.onepiecetcg.data.catalog

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.data.db.CardEntity
import com.chezley.onepiecetcg.data.db.CardSetEntity
import java.io.IOException
import org.json.JSONException
import org.json.JSONObject

private const val CATALOG_ASSET_DIR = "catalog"
private const val LOG_TAG = "CatalogLoader"

/** Decoded shape of one card in a bundled catalog-set JSON file (see assets/catalog/OP01.json). */
data class CatalogCardDto(
    val id: String,
    val name: String,
    val cardNumber: String,
    val rarity: String,
    val cost: Int?,
    val power: Int?,
    val attribute: String?,
    val type: String?,
    val imageUrl: String?,
)

data class CatalogSetDto(val code: String, val name: String)

data class CatalogDataset(val set: CatalogSetDto, val cards: List<CatalogCardDto>)

class CatalogLoaderException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Discovers every bundled catalog-set JSON file under `assets/catalog/` and
 * seeds the local `cards`/`card_sets` tables from all of them, mirroring the
 * iOS `CatalogLoader` (OnePieceTCG/OnePieceTCG/Catalog/CatalogLoader.swift)
 * so shipping an additional set is just adding another JSON file to
 * assets/catalog/ - no loader changes.
 *
 * Seeding is idempotent per set: cards already present (matched by `id`) are
 * left untouched, so relaunching, or adding a new set file in a future
 * build, only inserts what's missing and never duplicates existing data. A
 * set file that fails to load or parse is logged and skipped rather than
 * blocking the other sets or crashing.
 */
object CatalogLoader {

    /**
     * Base names (without extension) of every catalog-set JSON file bundled
     * under `assets/catalog/`, sorted for a deterministic seeding order.
     */
    fun discoverDatasetResourceNames(assets: AssetManager): List<String> =
        (assets.list(CATALOG_ASSET_DIR) ?: emptyArray())
            .filter { it.endsWith(".json") }
            .map { it.removeSuffix(".json") }
            .sorted()

    fun loadDataset(resourceName: String, assets: AssetManager): CatalogDataset {
        val json = try {
            assets.open("$CATALOG_ASSET_DIR/$resourceName.json").use { it.reader().readText() }
        } catch (e: IOException) {
            throw CatalogLoaderException("Catalog asset not found: $resourceName", e)
        }
        return try {
            parseDataset(json)
        } catch (e: JSONException) {
            throw CatalogLoaderException("Malformed catalog asset: $resourceName", e)
        }
    }

    internal fun parseDataset(json: String): CatalogDataset {
        val root = JSONObject(json)
        val setObj = root.getJSONObject("set")
        val set = CatalogSetDto(code = setObj.getString("code"), name = setObj.getString("name"))

        val cardsArray = root.getJSONArray("cards")
        val cards = buildList {
            for (i in 0 until cardsArray.length()) {
                val c = cardsArray.getJSONObject(i)
                add(
                    CatalogCardDto(
                        id = c.getString("id"),
                        name = c.getString("name"),
                        cardNumber = c.getString("cardNumber"),
                        rarity = c.getString("rarity"),
                        cost = c.optIntOrNull("cost"),
                        power = c.optIntOrNull("power"),
                        attribute = c.optStringOrNull("attribute"),
                        type = c.optStringOrNull("type"),
                        imageUrl = c.optStringOrNull("imageURL"),
                    ),
                )
            }
        }
        return CatalogDataset(set = set, cards = cards)
    }

    private fun JSONObject.optStringOrNull(name: String): String? =
        if (has(name) && !isNull(name)) getString(name) else null

    private fun JSONObject.optIntOrNull(name: String): Int? =
        if (has(name) && !isNull(name)) getInt(name) else null

    /**
     * Seeds the catalog from every set file discovered under
     * `assets/catalog/`. Returns the total number of new cards inserted
     * across all sets.
     *
     * Fetches the existing set codes/card IDs once up front (rather than
     * once per set file, as [seedDataset] does in isolation) and threads
     * them through each file via [insertDataset], which updates those sets
     * in place as it inserts — so a multi-file pass costs one query per
     * table instead of O(sets) queries as more sets get bundled (#11/#13).
     */
    suspend fun seedCatalog(context: Context, database: AppDatabase): Int {
        val existingSetCodes = database.cardSetDao().getAll().map { it.code }.toMutableSet()
        val existingCardIds = database.cardDao().getAll().map { it.id }.toMutableSet()

        var totalInserted = 0
        for (resourceName in discoverDatasetResourceNames(context.assets)) {
            totalInserted += try {
                val dataset = loadDataset(resourceName, context.assets)
                insertDataset(dataset, database, existingSetCodes, existingCardIds)
            } catch (e: CatalogLoaderException) {
                Log.e(LOG_TAG, "Skipping catalog set '$resourceName'", e)
                0
            }
        }
        return totalInserted
    }

    /**
     * Seeds the catalog from a single named set file. Exposed separately
     * from [seedCatalog] so a specific known set can be (re-)seeded, and so
     * tests can exercise one dataset without going through asset discovery.
     */
    suspend fun seedDataset(resourceName: String, assets: AssetManager, database: AppDatabase): Int {
        val dataset = loadDataset(resourceName, assets)
        val existingSetCodes = database.cardSetDao().getAll().map { it.code }.toMutableSet()
        val existingCardIds = database.cardDao().getAll().map { it.id }.toMutableSet()
        return insertDataset(dataset, database, existingSetCodes, existingCardIds)
    }

    /**
     * Inserts [dataset]'s set/cards that aren't already present in
     * [existingSetCodes]/[existingCardIds], updating both sets in place so
     * a caller looping over multiple datasets (see [seedCatalog]) never has
     * to re-query the store to know what's already there. Returns the
     * number of new cards inserted for this dataset.
     */
    private suspend fun insertDataset(
        dataset: CatalogDataset,
        database: AppDatabase,
        existingSetCodes: MutableSet<String>,
        existingCardIds: MutableSet<String>,
    ): Int {
        if (dataset.set.code !in existingSetCodes) {
            database.cardSetDao().insertAll(
                listOf(CardSetEntity(code = dataset.set.code, name = dataset.set.name, releaseDate = null)),
            )
            existingSetCodes += dataset.set.code
        }

        val newCards = dataset.cards.filter { it.id !in existingCardIds }
        if (newCards.isNotEmpty()) {
            database.cardDao().insertAll(
                newCards.map {
                    CardEntity(
                        id = it.id,
                        name = it.name,
                        setCode = dataset.set.code,
                        cardNumber = it.cardNumber,
                        rarity = it.rarity,
                        cost = it.cost,
                        power = it.power,
                        attribute = it.attribute,
                        type = it.type,
                        imageUrl = it.imageUrl,
                    )
                },
            )
            existingCardIds += newCards.map { it.id }
        }
        return newCards.size
    }
}
