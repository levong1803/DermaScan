package com.example.dermascan.ui.screens.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.example.dermascan.model.ChatMessage
import com.example.dermascan.ui.components.*
import com.example.dermascan.ui.navigation.Routes
import com.example.dermascan.ui.theme.*
import com.example.dermascan.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(appState: DermascanAppState, navController: NavHostController) {
    val user = appState.currentUser
    val scanHistory = appState.scanHistory.sortedByDescending { it.dateMillis }
    val currentMonth = nowMonth()
    val thisMonthCount = appState.scanHistory.count { monthOf(it.dateMillis) == currentMonth }
    ScreenColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        GradientHeader(
            title = "Hello, ${user?.name ?: "User"}",
            subtitle = "How's your skin today?",
            colors = listOf(Teal, Cyan),
            action = {
                IconButton(onClick = { navController.navigate(Routes.Notifications) }) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                }
            },
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                HeaderStatCard("Total Scans", appState.scanHistory.size.toString(), Icons.Default.CameraAlt, Color(0xFFDBEAFE), Blue)
                HeaderStatCard("This Month", thisMonthCount.toString(), Icons.Default.CalendarToday, Color(0xFFDCFCE7), Green)
                HeaderStatCard("Skin Score", "${appState.scanHistory.lastOrNull()?.score ?: 82}%", Icons.Default.Star, Color(0xFFF3E8FF), Purple)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("Quick Actions")
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            QuickActionCard("Scan Skin", listOf(Teal, Cyan), Icons.Default.CameraAlt) { navController.navigate(Routes.Scan) }
            QuickActionCard("Progress", listOf(Blue, Purple), Icons.AutoMirrored.Filled.TrendingUp) { navController.navigate(Routes.Progress) }
            QuickActionCard("AI Chat", listOf(Purple, Pink), Icons.AutoMirrored.Filled.Chat) { navController.navigate(Routes.Chatbot) }
            QuickActionCard("Products", listOf(Orange, Color(0xFFEF4444)), Icons.Default.ShoppingCart) { navController.navigate(Routes.Products) }
        }
        Spacer(modifier = Modifier.height(26.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Recent Scans")
            TextButton(onClick = { navController.navigate(Routes.History) }) { Text("View All") }
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (scanHistory.isEmpty()) {
            EmptyState(Icons.Default.CameraAlt, "No scans yet", "Start your first scan to populate history", "Start Your First Scan") {
                navController.navigate(Routes.Scan)
            }
        } else {
            scanHistory.take(3).forEach { scan ->
                ScanListItem(scan, Icons.Default.CameraAlt) { navController.navigate(Routes.skinReport(scan.id)) }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ScanScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    var selectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var analyzing by rememberSaveable { mutableStateOf(false) }
    var complete by rememberSaveable { mutableStateOf(false) }
    var latestScanId by rememberSaveable { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri?.toString()
        complete = false
    }

    ScreenColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        GradientHeader(title = "Skin Scan", subtitle = "Take or upload a photo for AI analysis", colors = listOf(Teal, Cyan))
        Spacer(modifier = Modifier.height(24.dp))
        ElevatedCard(shape = RoundedCornerShape(30.dp), modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(320.dp).padding(18.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
                if (selectedImageUri != null) {
                    UriImage(selectedImageUri.orEmpty(), Modifier.fillMaxSize())
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(70.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No image selected", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Take a clear photo of your face in good lighting", color = Color.Gray)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
            Text(if (selectedImageUri == null) "Upload Photo" else "Change Image")
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (selectedImageUri != null && !complete) {
            Button(onClick = { analyzing = true }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                if (analyzing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analyzing...")
                    }
                } else {
                    Text("Start AI Analysis")
                }
            }
        }
        if (complete) {
            Spacer(modifier = Modifier.height(12.dp))
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Analysis complete. Redirecting to report...", color = Green, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    LaunchedEffect(analyzing) {
        if (!analyzing) return@LaunchedEffect
        delay(2500)
        val scan = appState.addGeneratedScan(selectedImageUri)
        latestScanId = scan.id
        analyzing = false
        complete = true
        showToast(context, "Analysis complete")
        delay(1000)
        navController.navigate(Routes.skinReport(latestScanId))
    }
}

@Composable
fun HistoryScreen(appState: DermascanAppState, navController: NavHostController) {
    var filter by rememberSaveable { mutableStateOf("all") }
    var query by rememberSaveable { mutableStateOf("") }
    val scans = appState.scanHistory.sortedByDescending { it.dateMillis }.filter { scan ->
        val matchFilter = when (filter) {
            "week" -> scan.dateMillis >= System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            "month" -> monthOf(scan.dateMillis) == nowMonth()
            else -> true
        }
        val matchQuery = query.isBlank() || scan.type.contains(query, ignoreCase = true)
        matchFilter && matchQuery
    }

    ScreenColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        GradientHeader(title = "Scan History", subtitle = "${appState.scanHistory.size} total scans", colors = listOf(Teal, Cyan), action = {
            TextButton(onClick = { navController.navigate(Routes.Compare) }) { Text("Compare", color = Color.White) }
        })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) }, placeholder = { Text("Search scans...") }, shape = RoundedCornerShape(18.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("all" to "All", "week" to "This Week", "month" to "This Month").forEach { item ->
                FilterChip(selected = filter == item.first, onClick = { filter = item.first }, label = { Text(item.second) })
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        if (scans.isEmpty()) {
            EmptyState(Icons.Default.Search, "No scans found", "Try a different search or create a new scan", "Start Scanning") {
                navController.navigate(Routes.Scan)
            }
        } else {
            scans.forEach { scan ->
                ScanListItem(scan, Icons.Default.CameraAlt) { navController.navigate(Routes.skinReport(scan.id)) }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatbotScreen() {
    val quickQuestions = listOf("What causes acne?", "Best products for dry skin", "How to reduce wrinkles?", "Sunscreen recommendations")
    val botResponses = listOf(
        "Based on your recent scan, focus on hydration and daily SPF.",
        "Acne can be triggered by hormones, stress, and product buildup.",
        "For dry skin, use a gentle cleanser, hyaluronic acid, and a richer moisturizer.",
        "Retinol and sun protection are the strongest long-term anti-aging basics.",
    )
    val messages = remember {
        mutableStateListOf(ChatMessage("1", "Hello. I'm your AI skincare assistant. I can help explain analysis results, suggest products, and answer routine questions.", false, "now"))
    }
    var input by rememberSaveable { mutableStateOf("") }
    var typing by rememberSaveable { mutableStateOf(false) }

    fun send(message: String) {
        if (message.isBlank()) return
        messages.add(ChatMessage(System.currentTimeMillis().toString(), message, true, "now"))
        input = ""
        typing = true
    }

    ScreenColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        GradientHeader(title = "AI Assistant", subtitle = "Online", colors = listOf(Purple, Pink))
        Spacer(modifier = Modifier.height(18.dp))
        messages.forEach { message ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
                ElevatedCard(shape = RoundedCornerShape(22.dp), colors = CardDefaults.elevatedCardColors(containerColor = if (message.fromUser) Teal else Color.White), modifier = Modifier.fillMaxWidth(0.82f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(message.text, color = if (message.fromUser) Color.White else Color.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(message.timestampLabel, color = if (message.fromUser) Color.White.copy(alpha = 0.7f) else Color.Gray, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        if (typing) {
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Assistant is typing...", color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (messages.size == 1) {
            SectionTitle("Quick Questions")
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                quickQuestions.forEach { question -> AssistChip(onClick = { send(question) }, label = { Text(question) }) }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
        AppTextField(
            label = "Ask me anything about skincare...",
            value = input,
            onValueChange = { input = it },
            leading = { Icon(Icons.Default.Chat, null) },
            trailing = {
                IconButton(onClick = { send(input) }, enabled = input.isNotBlank()) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = if (input.isNotBlank()) Teal else Color.Gray)
                }
            },
        )
    }

    LaunchedEffect(typing) {
        if (!typing) return@LaunchedEffect
        delay(1200)
        messages.add(ChatMessage(System.currentTimeMillis().toString(), botResponses.random(), false, "now"))
        typing = false
    }
}

@Composable
fun ProfileScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    val user = appState.currentUser
    ScreenColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        GradientHeader(
            title = user?.name ?: "User",
            subtitle = user?.email ?: "",
            colors = listOf(Teal, Cyan),
            action = {
                IconButton(onClick = { showToast(context, "Share link copied") }) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                }
            },
        )
        Spacer(modifier = Modifier.height(18.dp))
        ElevatedCard(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(84.dp).clip(CircleShape).background(Teal), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(user?.name ?: "User", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(user?.email ?: "No email", color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    ProfileStat("Total Scans", appState.scanHistory.size.toString())
                    ProfileStat("Skin Score", "${appState.scanHistory.lastOrNull()?.score ?: 82}%")
                    ProfileStat("Streak Days", "7")
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        listOf(
            Triple("Progress Tracking", Icons.AutoMirrored.Filled.TrendingUp, Routes.Progress),
            Triple("Product Suggestions", Icons.Default.ShoppingCart, Routes.Products),
            Triple("Notifications", Icons.Default.Notifications, Routes.Notifications),
            Triple("Settings", Icons.Default.Settings, Routes.Settings),
        ).forEach { item ->
            ListNavRow(item.first, item.second) { navController.navigate(item.third) }
            Spacer(modifier = Modifier.height(12.dp))
        }
        Button(onClick = {
            appState.logout()
            showToast(context, "Logged out")
            navController.navigate(Routes.Login) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(20.dp)) {
            Text("Logout")
        }
    }
}

@Composable
private fun RowScope.HeaderStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, background: Color, tint: Color) {
    Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha = 0.2f)).padding(14.dp)) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(background), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.82f), fontSize = 12.sp)
    }
}

@Composable
private fun QuickActionCard(title: String, colors: List<Color>, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(modifier = Modifier.width(156.dp).height(132.dp).clip(RoundedCornerShape(28.dp)).background(androidx.compose.ui.graphics.Brush.linearGradient(colors)).clickable(onClick = onClick).padding(18.dp)) {
        Column {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
