package com.example.dermascan.data.network

data class BackendProduct(
    val id: Int,
    val name: String,
    val brand: String,
    val price: Double,
    val rating: Double,
    val reviews: Int,
    val category: String,
    val skinType: String,
    val benefits: List<String>,
)

data class BackendProductsResponse(
    val products: List<BackendProduct>,
    val total: Int,
)

data class BackendUserProfile(
    val uid: String,
    val name: String? = null,
    val email: String? = null,
    val skinType: String? = null,
    val settings: Map<String, Any?>? = null,
)

data class BackendConditionResult(
    val name: String,
    val severity: String,
    val confidence: Int,
)

data class BackendScanRecord(
    val id: String,
    val dateMillis: Long,
    val imageUrl: String? = null,
    val type: String,
    val score: Int,
    val conditions: List<BackendConditionResult>,
    val recommendations: List<String>,
)

data class BackendScanHistoryResponse(
    val scans: List<BackendScanRecord>,
    val total: Int,
)

data class BackendChatRequest(
    val message: String,
    val chat_id: String? = null,
)

data class BackendChatMessage(
    val id: String,
    val text: String,
    val fromUser: Boolean,
    val timestamp: Long,
)

data class BackendChatResponse(
    val chat_id: String,
    val reply: BackendChatMessage,
)

data class BackendProfileUpdateRequest(
    val name: String? = null,
    val skinType: String? = null,
)

data class BackendRecommendedProductsResponse(
    val products: List<BackendProduct>,
    val total: Int,
    val based_on: String = "general",
    val scan_score: Int? = null,
    val conditions: List<String>? = null,
)

data class BackendChatHistoryItem(
    val chatId: String,
    val lastTimestamp: Long,
)

data class BackendChatHistoryResponse(
    val chats: List<BackendChatHistoryItem>,
)

data class BackendChatMessagesResponse(
    val chatId: String,
    val messages: List<BackendChatMessage>,
)
