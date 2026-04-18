package com.example.dermascan.ui.navigation

object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Permissions = "permissions"
    const val Login = "login"
    const val Register = "register"
    const val Verify = "verify"
    const val ForgotPassword = "forgot_password"
    const val Home = "home"
    const val Scan = "scan"
    const val History = "history"
    const val Chatbot = "chatbot"
    const val Profile = "profile"
    const val Settings = "settings"
    const val Recommendations = "recommendations"
    const val Products = "products"
    const val Progress = "progress"
    const val Compare = "compare"
    const val Notifications = "notifications"
    const val SkinReport = "skin_report/{scanId}"

    fun skinReport(scanId: String): String = "skin_report/$scanId"
}
