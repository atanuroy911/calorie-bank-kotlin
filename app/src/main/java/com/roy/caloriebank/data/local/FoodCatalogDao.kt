package com.roy.caloriebank.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FoodCatalogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<FoodCatalogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: FoodCatalogEntity)

    @Query("SELECT COUNT(*) FROM food_catalog")
    suspend fun count(): Int

    /** Recently/frequently used foods first, then alphabetical matches — recent-first is the
     * whole point: the user shouldn't have to retype "Chicken Breast" every day. */
    @Query(
        """
        SELECT * FROM food_catalog
        WHERE name LIKE '%' || :query || '%'
        ORDER BY useCount DESC, lastUsedAt DESC, name ASC
        LIMIT :limit
        """,
    )
    suspend fun search(query: String, limit: Int = 25): List<FoodCatalogEntity>

    @Query(
        """
        SELECT * FROM food_catalog
        WHERE lastUsedAt IS NOT NULL
        ORDER BY lastUsedAt DESC
        LIMIT :limit
        """,
    )
    suspend fun recent(limit: Int = 10): List<FoodCatalogEntity>

    @Query("SELECT * FROM food_catalog WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): FoodCatalogEntity?
}
