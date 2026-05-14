package com.example.dermascan.data.network

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface BackendApi {
    @GET("api/auth/me")
    suspend fun getCurrentUserProfile(): BackendUserProfile

    @GET("api/scan/history")
    suspend fun getScanHistory(): BackendScanHistoryResponse

    @Multipart
    @POST("api/scan/analyze")
    suspend fun analyzeScan(
        @Part file: MultipartBody.Part,
    ): BackendScanRecord

    @GET("api/products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
    ): BackendProductsResponse

    @POST("api/chat/message")
    suspend fun sendChatMessage(
        @Body body: BackendChatRequest,
    ): BackendChatResponse

    @POST("api/auth/profile")
    suspend fun updateProfile(
        @Body body: BackendProfileUpdateRequest,
    ): BackendUserProfile

    @GET("api/products/recommended")
    suspend fun getRecommendedProducts(): BackendRecommendedProductsResponse

    @GET("api/chat/history")
    suspend fun getChatHistory(): BackendChatHistoryResponse

    @GET("api/chat/{chatId}/messages")
    suspend fun getChatMessages(@Path("chatId") chatId: String): BackendChatMessagesResponse
}
