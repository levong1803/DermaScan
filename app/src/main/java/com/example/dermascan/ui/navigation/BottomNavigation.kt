package com.example.dermascan.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.dermascan.ui.theme.Teal

data class BottomItem(
    val route: String, 
    val labelEn: String, 
    val labelVi: String, 
    val icon: ImageVector
)

val bottomRoutes = setOf(Routes.Home, Routes.Scan, Routes.History, Routes.Chatbot, Routes.Profile)

private val bottomItems = listOf(
    BottomItem(Routes.Home, "Home", "Trang chủ", Icons.Default.Home),
    BottomItem(Routes.Scan, "Scan", "Quét da", Icons.Default.CameraAlt),
    BottomItem(Routes.History, "History", "Lịch sử", Icons.Default.History),
    BottomItem(Routes.Chatbot, "AI Chat", "Tư vấn", Icons.AutoMirrored.Filled.Chat),
    BottomItem(Routes.Profile, "Profile", "Tài khoản", Icons.Default.Person),
)

@Composable
fun AppBottomBar(navController: NavHostController, currentRoute: String) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        bottomItems.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                icon = { 
                    Icon(
                        item.icon, 
                        contentDescription = null,
                        tint = if (isSelected) Teal else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                label = { 
                    Text(
                        item.labelEn, 
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Teal else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Teal.copy(alpha = 0.1f)
                )
            )
        }
    }
}
