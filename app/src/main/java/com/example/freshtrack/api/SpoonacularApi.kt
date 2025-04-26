package com.example.freshtrack.api

import retrofit2.http.GET
import retrofit2.http.Query

data class RecipeResult(
    val id: Int,
    val title: String,
    val image: String,
    val usedIngredientCount: Int,
    val missedIngredientCount: Int
)

interface SpoonacularApi {
    @GET("recipes/findByIngredients")
    suspend fun findRecipes(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 10,
        @Query("ranking") ranking: Int = 1,
        @Query("ignorePantry") ignorePantry: Boolean = true,
        @Query("apiKey") apiKey: String
    ): List<RecipeResult>
}
