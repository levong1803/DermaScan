package com.example.dermascan.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dermascan.data.repository.AuthRepository
import com.example.dermascan.model.AppNotification
import com.example.dermascan.model.ConditionResult
import com.example.dermascan.model.ScanRecord
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class DermascanAppState(context: Context) {
    private val prefs = context.getSharedPreferences("dermascan_prefs", Context.MODE_PRIVATE)
    private val authRepository = AuthRepository()
    private val db = FirebaseFirestore.getInstance()

    // Theo dõi người dùng từ Firebase
    var firebaseUser by mutableStateOf<FirebaseUser?>(authRepository.currentUser)
        private set

    var hasSeenOnboarding by mutableStateOf(prefs.getBoolean("has_seen_onboarding", false))
        private set
    var cameraGranted by mutableStateOf(prefs.getBoolean("camera_granted", false))
        private set
    var notificationsEnabled by mutableStateOf(prefs.getBoolean("notifications_enabled", true))
        private set
    var darkMode by mutableStateOf(prefs.getBoolean("dark_mode", false))
        private set

    val scanHistory = mutableStateListOf<ScanRecord>()
    val favoriteProductIds = mutableStateListOf<Int>()
    val notifications = mutableStateListOf<AppNotification>()

    val isAuthenticated: Boolean get() = firebaseUser != null

    init {
        // Lắng nghe sự thay đổi trạng thái đăng nhập
        com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener { auth ->
            firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                loadUserData()
            } else {
                scanHistory.clear()
            }
        }
    }

    private fun loadUserData() {
        // Tương lai: Fetch scan history từ Firestore ở đây
    }

    fun finishOnboarding() {
        hasSeenOnboarding = true
        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
    }

    fun setPermission(permissionId: String, granted: Boolean) {
        if (permissionId == "camera") {
            cameraGranted = granted
            prefs.edit().putBoolean("camera_granted", granted).apply()
        }
    }

    fun toggleNotificationsEnabled(value: Boolean) {
        notificationsEnabled = value
        prefs.edit().putBoolean("notifications_enabled", value).apply()
    }

    fun toggleFavorite(productId: Int) {
        if (favoriteProductIds.contains(productId)) {
            favoriteProductIds.remove(productId)
        } else {
            favoriteProductIds.add(productId)
        }
    }

    fun markAllNotificationsRead() {
        val updated = notifications.map { it.copy(read = true) }
        notifications.clear()
        notifications.addAll(updated)
    }

    fun addGeneratedScan(imageUri: String?): ScanRecord {
        val score = Random.nextInt(70, 96)
        val record = ScanRecord(
            id = System.currentTimeMillis().toString(),
            dateMillis = System.currentTimeMillis(),
            imageUri = imageUri,
            type = "Facial Skin Analysis",
            score = score,
            conditions = listOf(
                ConditionResult("Acne", if (score >= 84) "Low" else "Moderate", Random.nextInt(84, 96)),
                ConditionResult("Fine Lines", if (score >= 76) "Low" else "Moderate", Random.nextInt(80, 92)),
                ConditionResult("Dark Spots", "Low", Random.nextInt(78, 90)),
            ),
            recommendations = listOf(
                "Use a gentle cleanser twice daily",
                "Apply sunscreen SPF 30+ every morning",
                if (score >= 84) "Maintain hydration with a lightweight serum" else "Introduce niacinamide to calm inflammation",
            ),
        )
        scanHistory.add(record)
        return record
    }

    fun logout() {
        authRepository.logout()
    }

    fun toggleDarkMode(value: Boolean) {
        darkMode = value
        prefs.edit().putBoolean("dark_mode", value).apply()
    }
    
    fun getAuthRepo() = authRepository
}
