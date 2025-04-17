package com.example.freshtrack.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object OpenFoodFactsApi {
    private val client = OkHttpClient()

    suspend fun fetchProductByBarcode(barcode: String): ProductData? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("OFF_API", "Failed to fetch product: ${response.code}")
                    return@withContext null
                }

                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)

                if (json.optInt("status") == 0) {
                    Log.w("OFF_API", "Product not found")
                    return@withContext null
                }

                val product = json.getJSONObject("product")

                ProductData(
                    barcode = barcode,
                    name = product.optString("product_name", "Unknown"),
                    brand = product.optString("brands", "Unknown"),
                    ingredients = product.optString("ingredients_text", ""),
                    allergens = product.optJSONArray("allergens_tags")?.let {
                        List(it.length()) { i -> it.getString(i) }
                    } ?: emptyList(),
                    category = product.optString("categories", ""),
                    quantity = product.optString("quantity", "")
                )
            } catch (e: Exception) {
                Log.e("OFF_API", "Error: ${e.message}", e)
                null
            }
        }
    }
}

data class ProductData(
    val barcode: String,
    val name: String,
    val brand: String,
    val ingredients: String,
    val allergens: List<String>,
    val category: String,
    val quantity: String
)
