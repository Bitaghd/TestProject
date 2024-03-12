package com.example.testproject.model

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductController {
    @GET("products")
    suspend fun getAllProducts(@Query("skip") skip: Int,
                               @Query("limit") limit: Int
                               ): Response<Products>
    @GET("products")
    suspend fun getProductsCount(): Response<Products>
}