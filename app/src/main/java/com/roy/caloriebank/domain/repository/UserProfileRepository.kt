package com.roy.caloriebank.domain.repository

import com.roy.caloriebank.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    suspend fun getProfile(userId: String): UserProfile?
    suspend fun saveProfile(profile: UserProfile)
    suspend fun updateProfile(profile: UserProfile)
    fun watchProfile(userId: String): Flow<UserProfile?>
}
