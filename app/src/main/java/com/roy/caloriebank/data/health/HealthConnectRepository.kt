package com.roy.caloriebank.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class HealthActivitySummary(val steps: Long, val activeCaloriesBurned: Double)

/**
 * Thin wrapper around the Health Connect client — lets users link their steps/active-calories
 * data from any app that writes to Health Connect (Google Fit, Fitbit, Samsung Health, etc.)
 * instead of only manual exercise entries.
 */
@Singleton
class HealthConnectRepository @Inject constructor() {

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
    )

    fun isAvailable(context: Context): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    fun getClient(context: Context): HealthConnectClient = HealthConnectClient.getOrCreate(context)

    suspend fun hasAllPermissions(client: HealthConnectClient): Boolean {
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readTodaySummary(client: HealthConnectClient): HealthActivitySummary {
        val zone = ZoneId.systemDefault()
        val startOfDay = LocalTime.MIDNIGHT.atDate(LocalDate.now()).atZone(zone).toInstant()
        val timeRange = TimeRangeFilter.between(startOfDay, Instant.now())

        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL, ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                timeRangeFilter = timeRange,
            ),
        )
        val steps = response[StepsRecord.COUNT_TOTAL] ?: 0L
        val calories = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0
        return HealthActivitySummary(steps, calories)
    }
}
