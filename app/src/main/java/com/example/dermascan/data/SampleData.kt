package com.example.dermascan.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import com.example.dermascan.model.AppNotification
import com.example.dermascan.model.ConditionResult
import com.example.dermascan.model.Product
import com.example.dermascan.model.ScanRecord
import com.example.dermascan.ui.theme.Blue
import com.example.dermascan.ui.theme.Green
import com.example.dermascan.ui.theme.Pink
import com.example.dermascan.ui.theme.Purple

fun sampleProducts(): List<Product> = listOf(
    Product(1, "Hyaluronic Acid Serum", "The Ordinary", 29.99, 4.8, 2845, "Serum", "All", listOf("Hydration", "Plumping")),
    Product(2, "Retinol 0.5% Night Cream", "CeraVe", 39.99, 4.7, 1923, "Moisturizer", "Normal to Dry", listOf("Anti-aging", "Fine Lines")),
    Product(3, "Vitamin C Brightening Serum", "La Roche-Posay", 49.99, 4.9, 3421, "Serum", "All", listOf("Brightening", "Dark Spots")),
    Product(4, "Gentle Hydrating Cleanser", "Cetaphil", 19.99, 4.6, 5623, "Cleanser", "Sensitive", listOf("Cleansing", "Soothing")),
    Product(5, "Mineral Sunscreen SPF 50", "Neutrogena", 24.99, 4.8, 2156, "Sunscreen", "All", listOf("UV Protection", "Non-greasy")),
    Product(6, "Niacinamide Toner", "Paula's Choice", 34.99, 4.7, 1789, "Toner", "Oily to Combination", listOf("Pore Refining", "Oil Control")),
)

fun sampleNotifications(): List<AppNotification> = listOf(
    AppNotification(1, "Weekly Progress Update", "Your skin score improved by 8% this week. Keep it up.", "2 hours ago", Green, Icons.Default.TrendingUp, false),
    AppNotification(2, "Time for Your Routine", "Evening skincare reminder. Don't forget your retinol.", "5 hours ago", Blue, Icons.Default.CalendarToday, false),
    AppNotification(3, "New Product Recommendation", "We found 3 new products based on your latest scan.", "1 day ago", Purple, Icons.Default.ShoppingCart, true),
    AppNotification(4, "AI Assistant Response", "Your assistant has fresh skincare tips based on your history.", "2 days ago", Pink, Icons.Default.Chat, true),
)

fun demoScans(now: Long = System.currentTimeMillis()): List<ScanRecord> = listOf(
    ScanRecord(
        id = "1",
        dateMillis = now - 7L * 24 * 60 * 60 * 1000,
        type = "Facial Skin Analysis",
        score = 75,
        conditions = listOf(
            ConditionResult("Acne", "Moderate", 88),
            ConditionResult("Fine Lines", "Low", 82),
            ConditionResult("Dark Spots", "Low", 79),
        ),
        recommendations = listOf(
            "Use a gentle cleanser twice daily",
            "Apply sunscreen SPF 30+ daily",
            "Consider salicylic acid treatment for acne",
        ),
    ),
    ScanRecord(
        id = "2",
        dateMillis = now - 3L * 24 * 60 * 60 * 1000,
        type = "Facial Skin Analysis",
        score = 78,
        conditions = listOf(
            ConditionResult("Acne", "Low", 90),
            ConditionResult("Fine Lines", "Low", 85),
            ConditionResult("Dark Spots", "Low", 83),
        ),
        recommendations = listOf(
            "Continue current routine",
            "Add vitamin C serum in the morning",
            "Keep up with sun protection",
        ),
    ),
    ScanRecord(
        id = "3",
        dateMillis = now,
        type = "Facial Skin Analysis",
        score = 82,
        conditions = listOf(
            ConditionResult("Acne", "Low", 94),
            ConditionResult("Fine Lines", "Low", 87),
            ConditionResult("Dark Spots", "Low", 85),
        ),
        recommendations = listOf(
            "Excellent progress. Maintain current routine",
            "Consider adding hyaluronic acid for hydration",
            "Continue sun protection daily",
        ),
    ),
)
