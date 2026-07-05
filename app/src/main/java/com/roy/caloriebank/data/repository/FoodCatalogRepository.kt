package com.roy.caloriebank.data.repository

import android.content.Context
import com.roy.caloriebank.data.local.FoodCatalogDao
import com.roy.caloriebank.data.local.FoodCatalogEntity
import com.roy.caloriebank.data.local.FoodCatalogSource
import com.roy.caloriebank.data.remote.OpenFoodFactsApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class SeedFood(
    val name: String,
    val servingDescription: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double = 0.0,
    val sugarG: Double = 0.0,
)

/**
 * Search-as-you-type food lookup with three tiers, cheapest first: (1) the bundled starter
 * catalog + anything the user has ever logged before — instant, offline, and where "recent foods"
 * come from so people stop retyping the same breakfast every day — then (2) Open Food Facts, only
 * when nothing local matches, and rate-limited well under OFF's 10 req/min search quota so a
 * search-as-you-type UI can never get an IP banned.
 */
@Singleton
class FoodCatalogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: FoodCatalogDao,
    private val api: OpenFoodFactsApi,
) {
    private val seedMutex = Mutex()
    private var seeded = false

    private val rateLimitMutex = Mutex()
    private var lastOffCallAtMs = 0L
    private val minIntervalMs = 6_500L // 10 req/min allowed; this keeps us well under it.

    suspend fun seedIfNeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return@withLock
            if (dao.count() == 0) {
                withContext(Dispatchers.IO) {
                    val text = context.assets.open("food_seed.json").bufferedReader().use { it.readText() }
                    val seedFoods = Json { ignoreUnknownKeys = true }.decodeFromString<List<SeedFood>>(text)
                    dao.insertAll(
                        seedFoods.map { f ->
                            FoodCatalogEntity(
                                id = UUID.randomUUID().toString(),
                                name = f.name,
                                servingDescription = f.servingDescription,
                                calories = f.calories,
                                proteinG = f.proteinG,
                                carbsG = f.carbsG,
                                fatG = f.fatG,
                                fiberG = f.fiberG,
                                sugarG = f.sugarG,
                                source = FoodCatalogSource.LOCAL,
                            )
                        },
                    )
                }
            }
            seeded = true
        }
    }

    suspend fun searchLocal(query: String): List<FoodCatalogEntity> {
        if (query.isBlank()) return dao.recent(10)
        return dao.search(query.trim())
    }

    suspend fun getRecentFoods(limit: Int = 10): List<FoodCatalogEntity> = dao.recent(limit)

    /** Returns null (not empty) when skipped for rate-limiting, so the UI can tell "no results"
     * apart from "didn't even try this time". */
    suspend fun searchOpenFoodFacts(query: String): List<FoodCatalogEntity>? {
        if (query.isBlank()) return null
        val allowed = rateLimitMutex.withLock {
            val now = System.currentTimeMillis()
            if (now - lastOffCallAtMs < minIntervalMs) {
                false
            } else {
                lastOffCallAtMs = now
                true
            }
        }
        if (!allowed) return null

        return try {
            val response = withContext(Dispatchers.IO) { api.search(query) }
            val entries = response.products.mapNotNull { p ->
                val name = p.productName?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val n = p.nutriments ?: return@mapNotNull null
                val calories = (n.energyKcal100g ?: return@mapNotNull null).toInt()
                FoodCatalogEntity(
                    id = UUID.randomUUID().toString(),
                    name = if (p.brands != null) "$name (${p.brands})" else name,
                    servingDescription = "100g",
                    calories = calories,
                    proteinG = n.proteins100g ?: 0.0,
                    carbsG = n.carbohydrates100g ?: 0.0,
                    fatG = n.fat100g ?: 0.0,
                    fiberG = n.fiber100g ?: 0.0,
                    sugarG = n.sugars100g ?: 0.0,
                    source = FoodCatalogSource.OPEN_FOOD_FACTS,
                )
            }
            if (entries.isNotEmpty()) dao.insertAll(entries) // cache so we don't re-hit the network next time
            entries
        } catch (e: Exception) {
            null
        }
    }

    /** Called whenever a food is actually logged, so it rises to the top of "recent" next time. */
    suspend fun recordUsage(
        name: String,
        servingDescription: String,
        calories: Int,
        proteinG: Double,
        carbsG: Double,
        fatG: Double,
        fiberG: Double = 0.0,
        sugarG: Double = 0.0,
    ) {
        val existing = dao.findByName(name)
        val now = System.currentTimeMillis()
        if (existing != null) {
            dao.upsert(existing.copy(lastUsedAt = now, useCount = existing.useCount + 1))
        } else {
            dao.upsert(
                FoodCatalogEntity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    servingDescription = servingDescription,
                    calories = calories,
                    proteinG = proteinG,
                    carbsG = carbsG,
                    fatG = fatG,
                    fiberG = fiberG,
                    sugarG = sugarG,
                    source = FoodCatalogSource.USER,
                    lastUsedAt = now,
                    useCount = 1,
                ),
            )
        }
    }
}
