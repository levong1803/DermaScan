package com.example.dermascan.data.network

import android.content.Context
import android.net.Uri
import com.example.dermascan.model.ChatMessage
import com.example.dermascan.model.ConditionResult
import com.example.dermascan.model.Product
import com.example.dermascan.model.ScanRecord
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackendRepository(
    private val context: Context,
) {
    private val auth = FirebaseAuth.getInstance()

    private val api: BackendApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val user = auth.currentUser ?: throw IOException("Missing Firebase user")
                val token = Tasks.await(user.getIdToken(false)).token
                    ?: throw IOException("Missing Firebase ID token")
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApi::class.java)
    }

    suspend fun getCurrentUserProfile(): BackendUserProfile = api.getCurrentUserProfile()

    suspend fun getScanHistory(): List<ScanRecord> = api.getScanHistory().scans.map { it.toScanRecord() }

    suspend fun analyzeScan(imageUri: String): ScanRecord = withContext(Dispatchers.IO) {
        val uri = Uri.parse(imageUri)
        val contentResolver = context.contentResolver
        
        // Xác định MIME type và lấy phần mở rộng phù hợp
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = if (mimeType == "image/png") ".png" else ".jpg"
        
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Unable to read selected image")

        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        // Thêm phần mở rộng vào tên file để Backend dễ nhận diện
        val part = MultipartBody.Part.createFormData("file", "scan_image_$extension", requestBody)
        
        try {
            api.analyzeScan(part).toScanRecord()
        } catch (e: Exception) {
            // Ném lỗi chi tiết hơn để debug
            throw Exception("Server Error: ${e.localizedMessage}")
        }
    }

    suspend fun getProducts(category: String? = null): List<Product> {
        return api.getProducts(category = category).products.map {
            Product(
                id = it.id,
                name = it.name,
                brand = it.brand,
                price = it.price,
                rating = it.rating,
                reviews = it.reviews,
                category = it.category,
                skinType = it.skinType,
                benefits = it.benefits,
            )
        }
    }

    suspend fun sendChatMessage(message: String, chatId: String? = null): Pair<String, ChatMessage> {
        val response = api.sendChatMessage(
            BackendChatRequest(
                message = message,
                chat_id = chatId,
            )
        )
        return response.chat_id to response.reply.toChatMessage()
    }

    suspend fun updateProfile(name: String? = null, skinType: String? = null): BackendUserProfile {
        return api.updateProfile(BackendProfileUpdateRequest(name = name, skinType = skinType))
    }

    suspend fun getRecommendedProducts(): List<Product> {
        return api.getRecommendedProducts().products.map {
            Product(
                id = it.id,
                name = it.name,
                brand = it.brand,
                price = it.price,
                rating = it.rating,
                reviews = it.reviews,
                category = it.category,
                skinType = it.skinType,
                benefits = it.benefits,
            )
        }
    }

    suspend fun getLatestChatId(): String? {
        val history = api.getChatHistory()
        return history.chats.firstOrNull()?.chatId
    }

    suspend fun getChatMessages(chatId: String): List<ChatMessage> {
        return api.getChatMessages(chatId).messages.map { it.toChatMessage() }
    }

    private fun BackendScanRecord.toScanRecord(): ScanRecord {
        return ScanRecord(
            id = id,
            dateMillis = dateMillis,
            imageUri = imageUrl,
            type = type,
            score = score,
            conditions = conditions.map {
                ConditionResult(
                    name = it.name,
                    severity = it.severity,
                    confidence = it.confidence,
                )
            },
            recommendations = recommendations,
        )
    }

    private fun BackendChatMessage.toChatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            text = text,
            fromUser = fromUser,
            timestampLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp)),
        )
    }

    companion object {
        // Đảm bảo server đang chạy tại đây
        // Trả về 10.0.2.2 để dùng cho máy ảo Emulator
        private const val BASE_URL = "http://10.0.2.2:8000/"
    }
}
