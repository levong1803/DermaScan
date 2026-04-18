package com.example.dermascan.util

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun scoreColor(score: Int): Color = when {
    score >= 80 -> Color(0xFF16A34A)
    score >= 60 -> Color(0xFFD97706)
    else -> Color(0xFFEF4444)
}

fun scoreSummary(score: Int): String = when {
    score >= 80 -> "Excellent. Your skin is in great condition."
    score >= 60 -> "Good. There are still a few areas to improve."
    else -> "Needs attention. Follow the recommendations below."
}

fun formatDate(timestamp: Long): String = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(timestamp))

fun shortDate(timestamp: Long): String = SimpleDateFormat("MM/dd", Locale.US).format(Date(timestamp))

fun monthOf(timestamp: Long): Int = Calendar.getInstance().apply {
    timeInMillis = timestamp
}.get(Calendar.MONTH)

fun nowMonth(): Int = monthOf(System.currentTimeMillis())

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
