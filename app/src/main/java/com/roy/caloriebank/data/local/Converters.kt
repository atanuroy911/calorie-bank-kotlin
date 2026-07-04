package com.roy.caloriebank.data.local

import androidx.room.TypeConverter
import com.roy.caloriebank.domain.model.FoodItem
import com.roy.caloriebank.domain.model.Macros
import com.roy.caloriebank.domain.model.Micros
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

private val json = Json { ignoreUnknownKeys = true }

class Converters {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(millis: Long?): Instant? = millis?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromMacros(macros: Macros?): String = json.encodeToString(macros ?: Macros())

    @TypeConverter
    fun toMacros(value: String?): Macros =
        if (value.isNullOrBlank() || value == "{}") Macros() else json.decodeFromString(value)

    @TypeConverter
    fun fromMicros(micros: Micros?): String = json.encodeToString(micros ?: Micros())

    @TypeConverter
    fun toMicros(value: String?): Micros =
        if (value.isNullOrBlank() || value == "{}") Micros() else json.decodeFromString(value)

    @TypeConverter
    fun fromFoodItemList(items: List<FoodItem>?): String = json.encodeToString(items ?: emptyList())

    @TypeConverter
    fun toFoodItemList(value: String?): List<FoodItem> =
        if (value.isNullOrBlank()) emptyList() else json.decodeFromString(value)
}
