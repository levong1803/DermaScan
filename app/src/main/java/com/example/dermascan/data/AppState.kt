package com.example.dermascan.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dermascan.model.AppNotification
import com.example.dermascan.model.ConditionResult
import com.example.dermascan.model.ScanRecord
import com.example.dermascan.model.User
import kotlin.random.Random

class DermascanAppState(context: Context) {
    private val prefs = context.getSharedPreferences("dermascan_prefs", Context.MODE_PRIVATE)

    var hasSeenOnboarding by mutableStateOf(prefs.getBoolean("has_seen_onboarding", false))
        private set
    var cameraGranted by mutableStateOf(prefs.getBoolean("camera_granted", false))
        private set
    var notificationsGranted by mutableStateOf(prefs.getBoolean("notifications_granted", false))
        private set
    var locationGranted by mutableStateOf(prefs.getBoolean("location_granted", false))
        private set
    var notificationsEnabled by mutableStateOf(prefs.getBoolean("notifications_enabled", true))
        private set
    var darkMode by mutableStateOf(prefs.getBoolean("dark_mode", false))
        private set
    var authToken by mutableStateOf(prefs.getString("auth_token", "") ?: "")
        private set
    var currentUser by mutableStateOf(loadSessionUser())
        private set
    var pendingVerification by mutableStateOf<User?>(null)
        private set

    val users = mutableStateListOf<User>()
    val scanHistory = mutableStateListOf<ScanRecord>()
    val favoriteProductIds = mutableStateListOf<Int>()
    val notifications = mutableStateListOf<AppNotification>().apply { addAll(sampleNotifications()) }

    val isAuthenticated: Boolean
        get() = authToken.isNotBlank() && currentUser != null

    init {
        if (currentUser?.email == "demo@dermascan.com") {
            seedDemoScansIfNeeded()
        }
    }

    fun finishOnboarding() {
        hasSeenOnboarding = true
        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
    }

    fun setPermission(permissionId: String, granted: Boolean) {
        when (permissionId) {
            "camera" -> {
                cameraGranted = granted
                prefs.edit().putBoolean("camera_granted", granted).apply()
            }
            "notifications" -> {
                notificationsGranted = granted
                prefs.edit().putBoolean("notifications_granted", granted).apply()
            }
            "location" -> {
                locationGranted = granted
                prefs.edit().putBoolean("location_granted", granted).apply()
            }
        }
    }

    fun login(email: String, password: String): String? {
        if (email == "demo@dermascan.com" && password == "demo123") {
            seedDemoScansIfNeeded()
            saveSession(User("demo-user", "Demo User", email), "demo-${System.currentTimeMillis()}")
            return null
        }
        val matched = users.firstOrNull { it.email.equals(email, true) && it.password == password }
            ?: return "Sai email hoặc mật khẩu"
        saveSession(matched, "token-${System.currentTimeMillis()}")
        return null
    }

    fun register(name: String, email: String, password: String): String? {
        if (users.any { it.email.equals(email, true) }) return "Email đã được đăng ký"
        val user = User("user-${System.currentTimeMillis()}", name, email, password, false)
        users.add(user)
        pendingVerification = user
        return null
    }

    fun verifyOtp(otp: String): String? {
        if (otp != "123456") return "OTP không hợp lệ. Dùng 123456 cho demo."
        val pending = pendingVerification ?: return "Không có tài khoản chờ xác minh"
        val verified = pending.copy(verified = true)
        val index = users.indexOfFirst { it.id == pending.id }
        if (index >= 0) users[index] = verified
        pendingVerification = null
        saveSession(verified, "token-${System.currentTimeMillis()}")
        return null
    }

    fun logout() {
        authToken = ""
        currentUser = null
        prefs.edit().remove("auth_token").remove("user_name").remove("user_email").apply()
    }

    fun toggleNotificationsEnabled(value: Boolean) {
        notificationsEnabled = value
        prefs.edit().putBoolean("notifications_enabled", value).apply()
    }

    fun toggleDarkMode(value: Boolean) {
        darkMode = value
        prefs.edit().putBoolean("dark_mode", value).apply()
    }

    fun toggleFavorite(productId: Int) {
        if (favoriteProductIds.contains(productId)) favoriteProductIds.remove(productId) else favoriteProductIds.add(productId)
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

    private fun saveSession(user: User, token: String) {
        authToken = token
        currentUser = user
        prefs.edit().putString("auth_token", token).putString("user_name", user.name).putString("user_email", user.email).apply()
    }

    private fun loadSessionUser(): User? {
        val name = prefs.getString("user_name", null)
        val email = prefs.getString("user_email", null)
        return if (name.isNullOrBlank() || email.isNullOrBlank()) null else User("persisted", name, email)
    }

    private fun seedDemoScansIfNeeded() {
        if (scanHistory.isNotEmpty()) return
        scanHistory.addAll(demoScans())
    }
}
