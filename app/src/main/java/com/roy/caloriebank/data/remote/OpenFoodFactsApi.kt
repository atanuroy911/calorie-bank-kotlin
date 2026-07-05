package com.roy.caloriebank.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("cgi/search.pl")
    suspend fun search(
        @Query("search_terms") query: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 15,
    ): OffSearchResponse
}

@Serializable
data class OffSearchResponse(
    @SerialName("products") val products: List<OffProduct> = emptyList(),
)

@Serializable
data class OffProduct(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("brands") val brands: String? = null,
    @SerialName("serving_size") val servingSize: String? = null,
    @SerialName("nutriments") val nutriments: OffNutriments? = null,
)

@Serializable
data class OffNutriments(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("energy-kcal_serving") val energyKcalServing: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("fiber_100g") val fiber100g: Double? = null,
    @SerialName("sugars_100g") val sugars100g: Double? = null,
)
