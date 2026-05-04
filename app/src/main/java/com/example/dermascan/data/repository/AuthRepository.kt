package com.example.dermascan.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    // Đăng ký, cập nhật tên và gửi mail xác thực
    suspend fun signUpWithEmail(name: String, email: String, pass: String) {
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        
        // Cập nhật tên hiển thị
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        result.user?.updateProfile(profileUpdates)?.await()
        
        // Gửi mail xác thực
        result.user?.sendEmailVerification()?.await()
        
        // Đăng xuất ngay để buộc người dùng phải xác thực rồi mới Login
        auth.signOut()
    }

    // Đăng nhập và kiểm tra xác thực email
    suspend fun signInWithEmail(email: String, pass: String): FirebaseUser? {
        val result = auth.signInWithEmailAndPassword(email, pass).await()
        val user = result.user
        
        if (user != null && !user.isEmailVerified) {
            auth.signOut()
            throw Exception("Vui lòng xác thực email trước khi đăng nhập. Hãy kiểm tra hộp thư của bạn.")
        }
        return user
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    suspend fun signInWithFacebook(accessToken: String) {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        auth.signInWithCredential(credential).await()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun logout() {
        auth.signOut()
    }
}
