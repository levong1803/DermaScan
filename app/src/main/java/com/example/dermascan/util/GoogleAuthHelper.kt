package com.example.dermascan.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleAuthHelper(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)

    // Hàm tìm Activity từ Context để tránh crash khi gọi bảng chọn tài khoản
    private fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }

    suspend fun signIn(): String? {
        // Sử dụng mã Web Client ID thực tế từ Firebase của bạn
        val webClientId = "617376899244-766fprh76180dt9khpvuh30o5qvpf8ps.apps.googleusercontent.com" 
        val activity = findActivity(context)

        if (activity == null) {
            Log.e("GoogleAuth", "Lỗi: Không tìm thấy Activity Context để hiển thị bảng chọn tài khoản.")
            return null
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Hiện mọi tài khoản Google có trên máy
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false) // Bắt buộc người dùng phải chọn (tránh lỗi tự động chọn sai)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            // Gọi bảng chọn tài khoản bằng Activity Context chuẩn
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleIdTokenCredential.idToken
            } else {
                Log.e("GoogleAuth", "Loại xác thực không khớp: ${credential.type}")
                null
            }
        } catch (e: Exception) {
            // Bắt mọi lỗi (bao gồm cả lỗi người dùng hủy bỏ) để app không bị crash
            Log.e("GoogleAuth", "Lỗi Google: ${e.message}")
            null 
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Lỗi đăng xuất: ${e.message}")
        }
    }
}
