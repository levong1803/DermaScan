package com.example.dermascan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dermascan.data.DermascanAppState
import com.example.dermascan.ui.screens.auth.ForgotPasswordScreen
import com.example.dermascan.ui.screens.auth.LoginScreen
import com.example.dermascan.ui.screens.auth.RegisterScreen
import com.example.dermascan.ui.screens.auth.VerifyScreen
import com.example.dermascan.ui.screens.feature.CompareScreen
import com.example.dermascan.ui.screens.feature.NotificationsScreen
import com.example.dermascan.ui.screens.feature.ProductsScreen
import com.example.dermascan.ui.screens.feature.ProgressScreen
import com.example.dermascan.ui.screens.feature.RecommendationsScreen
import com.example.dermascan.ui.screens.feature.SettingsScreen
import com.example.dermascan.ui.screens.feature.SkinReportScreen
import com.example.dermascan.ui.screens.main.ChatbotScreen
import com.example.dermascan.ui.screens.main.HistoryScreen
import com.example.dermascan.ui.screens.main.HomeScreen
import com.example.dermascan.ui.screens.main.ProfileScreen
import com.example.dermascan.ui.screens.main.ScanScreen
import com.example.dermascan.ui.screens.startup.OnboardingScreen
import com.example.dermascan.ui.screens.startup.PermissionScreen
import com.example.dermascan.ui.screens.startup.SplashScreen

@Composable
fun AppNavGraph(navController: NavHostController, appState: DermascanAppState) {
    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable(Routes.Splash) { SplashScreen(appState, navController) }
        composable(Routes.Onboarding) { OnboardingScreen(appState, navController) }
        composable(Routes.Permissions) { PermissionScreen(appState, navController) }
        composable(Routes.Login) { LoginScreen(appState, navController) }
        composable(Routes.Register) { RegisterScreen(appState, navController) }
        composable(Routes.Verify) { VerifyScreen(appState, navController) }
        composable(Routes.ForgotPassword) { ForgotPasswordScreen(navController) }
        composable(Routes.Home) { HomeScreen(appState, navController) }
        composable(Routes.Scan) { ScanScreen(appState, navController) }
        composable(Routes.History) { HistoryScreen(appState, navController) }
        composable(Routes.Chatbot) { ChatbotScreen() }
        composable(Routes.Profile) { ProfileScreen(appState, navController) }
        composable(Routes.Settings) { SettingsScreen(appState, navController) }
        composable(Routes.Recommendations) { RecommendationsScreen(navController) }
        composable(Routes.Products) { ProductsScreen(appState, navController) }
        composable(Routes.Progress) { ProgressScreen(appState, navController) }
        composable(Routes.Compare) { CompareScreen(appState, navController) }
        composable(Routes.Notifications) { NotificationsScreen(appState, navController) }
        composable(route = Routes.SkinReport, arguments = listOf(navArgument("scanId") { type = NavType.StringType })) { entry ->
            SkinReportScreen(appState, navController, entry.arguments?.getString("scanId").orEmpty())
        }
    }
}
