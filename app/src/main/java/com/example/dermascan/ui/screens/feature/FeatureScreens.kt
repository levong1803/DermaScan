package com.example.dermascan.ui.screens.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dermascan.data.DermascanAppState
import com.example.dermascan.data.sampleProducts
import com.example.dermascan.ui.components.*
import com.example.dermascan.ui.navigation.Routes
import com.example.dermascan.ui.theme.*
import com.example.dermascan.util.*
import kotlin.math.max

@Composable
fun SettingsScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    ScreenColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        BackHeader("Settings", colors = listOf(Teal, Cyan), onBack = { navController.popBackStack() }, backIcon = Icons.AutoMirrored.Filled.ArrowBack)
        Spacer(modifier = Modifier.height(18.dp))
        SettingsGroup("Preferences") {
            SettingToggleRow("Notifications", Icons.Default.Notifications, appState.notificationsEnabled) {
                appState.toggleNotificationsEnabled(it)
                showToast(context, if (it) "Notifications enabled" else "Notifications disabled")
            }
            SettingToggleRow("Dark Mode", Icons.Default.DarkMode, appState.darkMode) {
                appState.toggleDarkMode(it)
                showToast(context, if (it) "Dark mode enabled" else "Dark mode disabled")
            }
            SettingStaticRow("Language", Icons.Default.Language, "English")
        }
        Spacer(modifier = Modifier.height(18.dp))
        SettingsGroup("Privacy & Security") {
            SettingStaticRow("Change Password", Icons.Default.Lock)
            SettingStaticRow("Privacy Policy", Icons.Default.Security)
            SettingStaticRow("Data Management", Icons.Default.Storage)
        }
        Spacer(modifier = Modifier.height(18.dp))
        SettingsGroup("Support") {
            SettingStaticRow("Help Center", Icons.Default.HelpOutline)
            SettingStaticRow("App Version", Icons.Default.Settings, "v1.0.0")
        }
    }
}

@Composable
fun SkinReportScreen(appState: DermascanAppState, navController: NavHostController, scanId: String) {
    val context = LocalContext.current
    val scan = appState.scanHistory.firstOrNull { it.id == scanId }
    if (scan == null) {
        ScreenColumn { EmptyState(Icons.Default.Warning, "Report Not Found", "The selected scan report does not exist", "Go to History") { navController.navigate(Routes.History) } }
        return
    }
    ScreenColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        BackHeader(
            title = "Skin Analysis Report",
            subtitle = formatDate(scan.dateMillis),
            colors = listOf(Teal, Cyan),
            actions = {
                IconButton(onClick = { showToast(context, "Share link copied") }) { Icon(Icons.Default.Share, contentDescription = null, tint = Color.White) }
                IconButton(onClick = { showToast(context, "Report downloaded") }) { Icon(Icons.Default.Download, contentDescription = null, tint = Color.White) }
            },
            onBack = { navController.popBackStack() },
            backIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )
        Spacer(modifier = Modifier.height(18.dp))
        if (scan.imageUri != null) {
            ElevatedCard(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
                UriImage(scan.imageUri, Modifier.fillMaxWidth().height(220.dp))
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
        ElevatedCard(shape = RoundedCornerShape(30.dp), colors = CardDefaults.elevatedCardColors(containerColor = Teal), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Overall Skin Health Score", color = Color.White.copy(alpha = 0.92f), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(scan.score.toString(), color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("/100", color = Color.White.copy(alpha = 0.85f), fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(progress = { scan.score / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Color.White, trackColor = Color.White.copy(alpha = 0.28f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(scoreSummary(scan.score), color = Color.White.copy(alpha = 0.9f))
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        InfoCard {
            Text("Detected Conditions", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(14.dp))
            scan.conditions.forEach { condition ->
                Text(condition.name, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SeverityBadge(condition.severity)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Confidence ${condition.confidence}%")
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = { condition.confidence / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Teal)
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        InfoCard {
            Text("Personalized Recommendations", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(14.dp))
            scan.recommendations.forEach { recommendation ->
                Row(modifier = Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(recommendation)
                }
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = { navController.navigate(Routes.Recommendations) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) { Text("View Detailed Recommendations") }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = { navController.navigate(Routes.Products) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) { Text("Recommended Products") }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = { navController.navigate(Routes.Progress) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
            Icon(Icons.Default.TrendingUp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Track Progress")
        }
    }
}

@Composable
fun CompareScreen(appState: DermascanAppState, navController: NavHostController) {
    var selected by remember { mutableStateOf<List<String>>(emptyList()) }
    val scans = appState.scanHistory.sortedByDescending { it.dateMillis }
    val compareScans = selected.mapNotNull { id -> scans.firstOrNull { it.id == id } }
    ScreenColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        BackHeader("Compare Scans", "Select 2 scans to compare", listOf(Blue, Purple), onBack = { navController.popBackStack() }, backIcon = Icons.AutoMirrored.Filled.ArrowBack)
        Spacer(modifier = Modifier.height(18.dp))
        if (compareScans.size == 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                compareScans.forEach { scan ->
                    ElevatedCard(shape = RoundedCornerShape(24.dp), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (scan.imageUri != null) {
                                UriImage(scan.imageUri, Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(16.dp)))
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            Text(formatDate(scan.dateMillis), color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${scan.score}%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = scoreColor(scan.score))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            ElevatedCard(shape = RoundedCornerShape(28.dp), colors = CardDefaults.elevatedCardColors(containerColor = Blue), modifier = Modifier.fillMaxWidth()) {
                val diff = compareScans[1].score - compareScans[0].score
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Score Change", color = Color.White.copy(alpha = 0.9f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (diff >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (diff >= 0) "+$diff%" else "$diff%", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                            Text(if (diff >= 0) "Great improvement" else "Needs attention", color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            InfoCard {
                Text("Condition Changes", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(14.dp))
                compareScans.first().conditions.zip(compareScans.last().conditions).forEach { (old, new) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(old.name, fontWeight = FontWeight.SemiBold)
                            Text("${old.severity} → ${new.severity}", color = Color.Gray)
                        }
                        SeverityBadge(new.severity)
                    }
                }
            }
        } else {
            Text("Selected: ${selected.size}/2", color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            scans.forEach { scan ->
                val isSelected = selected.contains(scan.id)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).border(if (isSelected) 2.dp else 0.dp, Blue, RoundedCornerShape(24.dp)).clickable {
                        selected = when {
                            isSelected -> selected - scan.id
                            selected.size < 2 -> selected + scan.id
                            else -> selected
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)).background(Teal.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                            if (scan.imageUri != null) UriImage(scan.imageUri, Modifier.fillMaxSize()) else Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Teal)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(scan.type, fontWeight = FontWeight.SemiBold)
                            Text(formatDate(scan.dateMillis), color = Color.Gray)
                        }
                        ScorePill(scan.score)
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(appState: DermascanAppState, navController: NavHostController) {
    val scans = appState.scanHistory.sortedBy { it.dateMillis }
    val currentScore = scans.lastOrNull()?.score ?: 0
    val firstScore = scans.firstOrNull()?.score ?: currentScore
    val improvement = max(currentScore - firstScore, 0)
    ScreenColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        BackHeader("Progress Tracking", colors = listOf(Blue, Purple), onBack = { navController.popBackStack() }, backIcon = Icons.AutoMirrored.Filled.ArrowBack)
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SmallStatCard("Current Score", "$currentScore", "+$improvement%")
            SmallStatCard("Total Scans", "${scans.size}", "+${scans.size}")
            SmallStatCard("Improvement", "$improvement%", "+$improvement%")
        }
        Spacer(modifier = Modifier.height(18.dp))
        InfoCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Skin Health Score", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Green)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Improving", color = Green, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ScoreChart(scans)
        }
    }
}

@Composable
fun RecommendationsScreen(navController: NavHostController) {
    val routines = listOf(
        "Morning Routine" to listOf("Gentle Cleanser", "Vitamin C Serum", "Moisturizer", "Sunscreen SPF 50+"),
        "Evening Routine" to listOf("Oil Cleanser", "Water Cleanser", "Retinol Serum", "Night Cream"),
    )
    ScreenColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        BackHeader("Recommendations", "Personalized skincare routine", listOf(Purple, Pink), onBack = { navController.popBackStack() }, backIcon = Icons.AutoMirrored.Filled.ArrowBack)
        Spacer(modifier = Modifier.height(18.dp))
        routines.forEach { routine ->
            InfoCard {
                Text(routine.first, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(12.dp))
                routine.second.forEachIndexed { index, step ->
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(Teal), contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(step, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
        InfoCard {
            Text("Recommended Ingredients", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(12.dp))
            listOf("Hyaluronic Acid" to "Deep hydration", "Retinol" to "Anti-aging", "Vitamin C" to "Brightening", "Niacinamide" to "Pore refining").forEach { ingredient ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(ingredient.first, fontWeight = FontWeight.SemiBold)
                        Text(ingredient.second, color = Color.Gray)
                    }
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Teal)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Button(onClick = { navController.navigate(Routes.Products) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
            Text("Shop Recommended Products")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductsScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    var filter by rememberSaveable { mutableStateOf("all") }
    val products = remember { sampleProducts() }
    val filtered = if (filter == "all") products else products.filter { it.category == filter }
    ScreenColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        BackHeader("Product Suggestions", "Based on your skin analysis", listOf(Orange, Color(0xFFEF4444)), onBack = { navController.popBackStack() }, backIcon = Icons.AutoMirrored.Filled.ArrowBack)
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("all", "Serum", "Moisturizer", "Cleanser", "Sunscreen", "Toner").forEach { category ->
                FilterChip(selected = filter == category, onClick = { filter = category }, label = { Text(if (category == "all") "All Products" else category) })
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        filtered.forEach { product ->
            ElevatedCard(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(onClick = {}, label = { Text(product.category) })
                        IconButton(onClick = {
                            appState.toggleFavorite(product.id)
                            showToast(context, if (appState.favoriteProductIds.contains(product.id)) "Added to favorites" else "Removed from favorites")
                        }) {
                            Icon(if (appState.favoriteProductIds.contains(product.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (appState.favoriteProductIds.contains(product.id)) Color(0xFFEF4444) else Color.Gray)
                        }
                    }
                    Text(product.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(product.brand, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${product.rating}", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("(${product.reviews} reviews)", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Best for: ${product.skinType}", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        product.benefits.forEach { benefit -> AssistChip(onClick = {}, label = { Text(benefit) }) }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("$${product.price}", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Button(onClick = { showToast(context, "${product.name} added to cart") }, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange)) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(appState: DermascanAppState, navController: NavHostController) {
    val unread = appState.notifications.count { !it.read }
    ScreenColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        BackHeader(
            title = "Notifications",
            subtitle = "$unread unread",
            colors = listOf(Teal, Cyan),
            actions = {
                if (unread > 0) {
                    TextButton(onClick = { appState.markAllNotificationsRead() }) { Text("Mark all read", color = Color.White) }
                }
            },
            onBack = { navController.popBackStack() },
            backIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )
        Spacer(modifier = Modifier.height(18.dp))
        if (appState.notifications.isEmpty()) {
            EmptyState(Icons.Default.Notifications, "No Notifications", "You're all caught up", "Back") { navController.popBackStack() }
        } else {
            appState.notifications.forEach { notification ->
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).border(if (!notification.read) 2.dp else 0.dp, Teal, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(notification.accent.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(notification.icon, contentDescription = null, tint = notification.accent)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notification.title, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(notification.message, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(notification.time, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
