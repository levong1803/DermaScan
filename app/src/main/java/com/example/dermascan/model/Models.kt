package com.example.dermascan.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String? = null,
    val verified: Boolean = true,
)

data class ConditionResult(
    val name: String,
    val severity: String,
    val confidence: Int,
)

data class ScanRecord(
    val id: String,
    val dateMillis: Long,
    val imageUri: String? = null,
    val type: String,
    val score: Int,
    val conditions: List<ConditionResult>,
    val recommendations: List<String>,
    val skinAge: Int? = null,
)

data class ChatMessage(
    val id: String,
    val text: String,
    val fromUser: Boolean,
    val timestampLabel: String,
)

data class Product(
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

data class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val accent: Color,
    val icon: ImageVector,
    val read: Boolean = false,
)
