package com.example.dermascan.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dermascan.data.network.BackendRepository
import com.example.dermascan.data.repository.AuthRepository
import com.example.dermascan.model.AppNotification
import com.example.dermascan.model.ChatMessage
import com.example.dermascan.model.Product
import com.example.dermascan.model.ScanRecord
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DermascanAppState(context: Context) {
    // Đổi sang v7 để reset hoàn toàn trạng thái tích chọn trên máy bạn
    private val prefs = context.getSharedPreferences("dermascan_prefs_v7", Context.MODE_PRIVATE)
    private val authRepository = AuthRepository()
    private val backendRepository = BackendRepository(context)
    private val db = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var firebaseUser by mutableStateOf<FirebaseUser?>(authRepository.currentUser)
        private set

    var hasSeenOnboarding by mutableStateOf(prefs.getBoolean("has_seen_onboarding", false))
        private set
    
    var cameraGranted by mutableStateOf(prefs.getBoolean("camera_granted", false))
        private set
    
    // GIẢI QUYẾT 1: Đổi mặc định thành false
    var notificationsEnabled by mutableStateOf(prefs.getBoolean("notifications_enabled", false)) 
        private set
    
    // GIẢI QUYẾT 2: Thêm trạng thái cho Vị trí
    var locationGranted by mutableStateOf(prefs.getBoolean("location_granted", false))
        private set

    var darkMode by mutableStateOf(prefs.getBoolean("dark_mode", false))
        private set

    var language by mutableStateOf(prefs.getString("language", "en") ?: "en")
        private set

    var backendUserName by mutableStateOf<String?>(null)
        private set

    var backendUserEmail by mutableStateOf<String?>(null)
        private set

    var loadingScanHistory by mutableStateOf(false)
        private set

    var scanErrorMessage by mutableStateOf<String?>(null)
        private set

    var loadingProducts by mutableStateOf(false)
        private set

    var productsErrorMessage by mutableStateOf<String?>(null)
        private set

    val scanHistory = mutableStateListOf<ScanRecord>()
    val products = mutableStateListOf<Product>()
    val favoriteProductIds = mutableStateListOf<Int>()
    val notifications = mutableStateListOf<AppNotification>()
    val recommendedProducts = mutableStateListOf<Product>()

    val isAuthenticated: Boolean get() = firebaseUser != null

    init {
        com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener { auth ->
            firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                scope.launch {
                    loadUserData()
                }
            } else {
                scanHistory.clear()
                backendUserName = null
                backendUserEmail = null
            }
        }
    }

    suspend fun loadUserData() {
        loadingScanHistory = true
        scanErrorMessage = null
        try {
            val profile = backendRepository.getCurrentUserProfile()
            backendUserName = profile.name
            backendUserEmail = profile.email

            val scans = backendRepository.getScanHistory()
            scanHistory.clear()
            scanHistory.addAll(scans)

            val backendProducts = backendRepository.getProducts()
            products.clear()
            products.addAll(backendProducts)

            val recProducts = try { backendRepository.getRecommendedProducts() } catch (_: Exception) { emptyList() }
            recommendedProducts.clear()
            recommendedProducts.addAll(recProducts)
        } catch (error: Exception) {
            scanErrorMessage = error.message
        } finally {
            loadingScanHistory = false
        }
    }

    fun finishOnboarding() {
        hasSeenOnboarding = true
        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
    }

    fun setPermission(permissionId: String, granted: Boolean) {
        // GIẢI QUYẾT 2: Lưu trạng thái cho cả 3 loại quyền
        when (permissionId) {
            "camera" -> {
                cameraGranted = granted
                prefs.edit().putBoolean("camera_granted", granted).apply()
            }
            "notifications" -> {
                notificationsEnabled = granted
                prefs.edit().putBoolean("notifications_enabled", granted).apply()
            }
            "location" -> {
                locationGranted = granted
                prefs.edit().putBoolean("location_granted", granted).apply()
            }
        }
    }

    // KHÔI PHỤC ĐỂ SỬA LỖI BUILD: Hàm đánh dấu đã đọc thông báo
    fun markAllNotificationsRead() {
        val updated = notifications.map { it.copy(read = true) }
        notifications.clear()
        notifications.addAll(updated)
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

    suspend fun analyzeScan(imageUri: String?): ScanRecord {
        require(!imageUri.isNullOrBlank()) { "Image is required for analysis" }
        val record = backendRepository.analyzeScan(imageUri).let { backendRecord ->
            if (backendRecord.imageUri.isNullOrBlank()) {
                backendRecord.copy(imageUri = imageUri)
            } else {
                backendRecord
            }
        }
        scanHistory.removeAll { it.id == record.id }
        scanHistory.add(0, record)
        return record
    }

    suspend fun loadProducts(category: String? = null) {
        loadingProducts = true
        productsErrorMessage = null
        try {
            val backendProducts = backendRepository.getProducts(category)
            products.clear()
            products.addAll(backendProducts)
        } catch (error: Exception) {
            productsErrorMessage = error.message
        } finally {
            loadingProducts = false
        }
    }

    suspend fun sendChatMessage(message: String, chatId: String? = null): Pair<String, ChatMessage> {
        return backendRepository.sendChatMessage(message, chatId)
    }

    suspend fun loadLatestChatMessages(): Pair<String, List<ChatMessage>>? {
        val chatId = backendRepository.getLatestChatId() ?: return null
        val messages = backendRepository.getChatMessages(chatId)
        return chatId to messages
    }

    suspend fun updateProfile(name: String) {
        val profile = backendRepository.updateProfile(name = name)
        backendUserName = profile.name
    }

    fun logout() {
        authRepository.logout()
    }

    fun toggleDarkMode(value: Boolean) {
        darkMode = value
        prefs.edit().putBoolean("dark_mode", value).apply()
    }

    fun setAppLanguage(langCode: String) {
        language = langCode
        prefs.edit().putString("language", langCode).apply()
    }
    
    fun getAuthRepo() = authRepository
}
