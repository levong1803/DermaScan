package com.example.dermascan.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

data class BottomItem(val route: String, val label: String, val icon: ImageVector)

val bottomRoutes = setOf(Routes.Home, Routes.Scan, Routes.History, Routes.Chatbot, Routes.Profile)

private val bottomItems = listOf(
    BottomItem(Routes.Home, "Home", Icons.Default.Home),
    BottomItem(Routes.Scan, "Scan", Icons.Default.CameraAlt),
    BottomItem(Routes.History, "History", Icons.Default.History),
    BottomItem(Routes.Chatbot, "Chat", Icons.AutoMirrored.Filled.Chat),
    BottomItem(Routes.Profile, "Profile", Icons.Default.Person),
)

@Composable
fun AppBottomBar(navController: NavHostController, currentRoute: String) {
    NavigationBar {
        bottomItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
            )
        }
    }
}
