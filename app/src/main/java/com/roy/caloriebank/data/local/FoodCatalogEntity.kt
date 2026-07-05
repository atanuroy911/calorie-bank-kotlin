package com.roy.caloriebank.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single searchable food entry: either from the bundled starter set ([Source.LOCAL]), cached
 * from an Open Food Facts lookup ([Source.OPEN_FOOD_FACTS]), or learned from something the user
 * actually logged ([Source.USER]). All three live in one table so search/"recent foods" doesn't
 * need to merge multiple sources at query time.
 */
@Entity(
    tableName = "food_catalog",
    indices = [Index(value = ["name"], name = "idx_catalog_name")],
)
data class FoodCatalogEntity(
    @PrimaryKey val id: String,
    val name: String,
    val servingDescription: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double = 0.0,
    val sugarG: Double = 0.0,
    val source: String,
    /** Null until the user actually logs this food once. */
    val lastUsedAt: Long? = null,
    val useCount: Int = 0,
)

object FoodCatalogSource {
    const val LOCAL = "local"
    const val OPEN_FOOD_FACTS = "off"
    const val USER = "user"
}
