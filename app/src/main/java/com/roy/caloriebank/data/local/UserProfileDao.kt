package com.roy.caloriebank.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    suspend fun getProfile(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    fun watchProfile(userId: String): Flow<UserProfileEntity?>

    @Upsert
    suspend fun saveProfile(profile: UserProfileEntity)
}
