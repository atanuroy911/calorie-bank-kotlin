package com.roy.caloriebank.data.repository

import com.roy.caloriebank.data.local.UserProfileDao
import com.roy.caloriebank.data.local.toDomain
import com.roy.caloriebank.data.local.toEntity
import com.roy.caloriebank.domain.model.UserProfile
import com.roy.caloriebank.domain.repository.UserProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserProfileRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao,
) : UserProfileRepository {
    override suspend fun getProfile(userId: String): UserProfile? = dao.getProfile(userId)?.toDomain()

    override suspend fun saveProfile(profile: UserProfile) = dao.saveProfile(profile.toEntity())

    override suspend fun updateProfile(profile: UserProfile) = dao.saveProfile(profile.toEntity())

    override fun watchProfile(userId: String): Flow<UserProfile?> =
        dao.watchProfile(userId).map { it?.toDomain() }
}
