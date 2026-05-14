package com.example.dermascan.ui.screens.main

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.dermascan.data.DermascanAppState
import com.example.dermascan.model.ChatMessage
import com.example.dermascan.ui.components.*
import com.example.dermascan.ui.navigation.Routes
import com.example.dermascan.ui.theme.*
import com.example.dermascan.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(appState: DermascanAppState, navController: NavHostController) {
    val userName = appState.backendUserName ?: appState.firebaseUser?.displayName ?: "User"
    val scanHistory = remember(appState.scanHistory.size) { 
        appState.scanHistory.sortedByDescending { it.dateMillis } 
    }
    val currentMonth = nowMonth()
    val thisMonthCount = remember(appState.scanHistory.size) {
        appState.scanHistory.count { monthOf(it.dateMillis) == currentMonth }
    }
    
    val isVi = appState.language == "vi"

    ScreenColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        GradientHeader(
            title = if (isVi) "Xin chào, $userName" else "Hello, $userName",
            subtitle = if (isVi) "Làn da của bạn hôm nay thế nào?" else "How's your skin today?",
            colors = listOf(Teal, Cyan),
            action = {
                Surface(
                    onClick = { navController.navigate(Routes.Notifications) },
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                    }
                }
            },
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                HeaderStatCard(if (isVi) "Tổng quét" else "Total", appState.scanHistory.size.toString(), Icons.Default.CameraAlt, Color(0xFFDBEAFE), Blue)
                HeaderStatCard(if (isVi) "Tháng này" else "Month", thisMonthCount.toString(), Icons.Default.CalendarToday, Color(0xFFDCFCE7), Green)
                HeaderStatCard(if (isVi) "Điểm da" else "Score", "${appState.scanHistory.lastOrNull()?.score ?: 0}%", Icons.Default.Star, Color(0xFFF3E8FF), Purple)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        SectionTitle(if (isVi) "Truy cập nhanh" else "Quick Actions")
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionCard(
                if (isVi) "Quét da" else "Scan Skin", 
                listOf(Teal, Cyan), 
                Icons.Default.CameraAlt,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.Scan) }
            
            QuickActionCard(
                if (isVi) "Tiến trình" else "Progress", 
                listOf(Blue, Purple), 
                Icons.AutoMirrored.Filled.TrendingUp,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.Progress) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionCard(
                if (isVi) "Tư vấn AI" else "AI Chat", 
                listOf(Purple, Pink), 
                Icons.AutoMirrored.Filled.Chat,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.Chatbot) }
            
            QuickActionCard(
                if (isVi) "Sản phẩm" else "Products", 
                listOf(Orange, Red), 
                Icons.Default.ShoppingCart,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.Products) }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(if (isVi) "Lượt quét gần đây" else "Recent Scans")
            TextButton(onClick = { navController.navigate(Routes.History) }) { 
                Text(if (isVi) "Tất cả" else "View All", color = Teal, fontWeight = FontWeight.Bold) 
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        if (appState.loadingScanHistory) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) { SkeletonBox(Modifier.fillMaxWidth().height(88.dp).clip(AppShapes.Card)) }
            }
        } else if (scanHistory.isEmpty()) {
            EmptyState(
                icon = Icons.Default.AutoAwesome, 
                title = if (isVi) "Bắt đầu hành trình" else "Start Journey", 
                subtitle = if (isVi) "Thực hiện lượt quét đầu tiên để AI phân tích sức khỏe làn da bạn" else "Your skin history is empty. Start an AI analysis to see results.", 
                actionLabel = if (isVi) "Quét ngay" else "Scan Now"
            ) {
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
    val isVi = appState.language == "vi"
    var selectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var analyzing by rememberSaveable { mutableStateOf(false) }
    var tempPhotoUriString by rememberSaveable { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedImageUri = uri.toString()
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUriString != null) {
            selectedImageUri = tempPhotoUriString
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Sau khi cấp quyền, tự động mở camera luôn
            appState.setPermission("camera", true)
        } else {
            showToast(context, if (isVi) "Cần quyền camera để chụp ảnh" else "Camera permission required")
        }
    }

    fun launchCamera() {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            val file = File(context.cacheDir, "camera_capture_${UUID.randomUUID()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            tempPhotoUriString = uri.toString()
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            showToast(context, if (isVi) "Lỗi hệ thống: ${e.localizedMessage}" else "System error: ${e.localizedMessage}")
        }
    }

    ScreenColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        BackHeader(
            title = if (isVi) "Phân tích AI" else "AI Analysis", 
            subtitle = if (isVi) "Cung cấp ảnh để chuyên gia AI hỗ trợ bạn" else "Provide a photo for AI expert support", 
            colors = listOf(Teal, Cyan),
            onBack = { navController.popBackStack() },
            backIcon = Icons.AutoMirrored.Filled.ArrowBack
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            shape = AppShapes.Card,
            color = Color(0xFFF9FAFB),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (selectedImageUri != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        UriImage(selectedImageUri.orEmpty(), Modifier.fillMaxSize())
                        if (analyzing) {
                            AIScanOverlay(modifier = Modifier.fillMaxSize())
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraEnhance, contentDescription = null, tint = Teal, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(if (isVi) "Chụp hoặc nạp ảnh" else "Take or Upload Photo", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            if (isVi) "Ảnh cần rõ nét, đủ ánh sáng và nhìn chính diện" else "Ensure your face is clear, well-lit, and facing forward.", 
                            color = Color.Gray, 
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { if (!analyzing) launchCamera() },
                modifier = Modifier.weight(1f).height(58.dp),
                shape = AppShapes.Button,
                enabled = !analyzing,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Teal)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Teal)
                Spacer(modifier = Modifier.width(10.dp))
                Text(if (isVi) "Chụp ảnh" else "Camera", color = Teal, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { if (!analyzing) galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).height(58.dp),
                shape = AppShapes.Button,
                enabled = !analyzing,
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(if (isVi) "Thư viện" else "Gallery", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        AnimatedVisibility(
            visible = selectedImageUri != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Button(
                onClick = { analyzing = true }, 
                modifier = Modifier.fillMaxWidth().height(60.dp), 
                shape = AppShapes.Button, 
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                enabled = !analyzing,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (analyzing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(if (isVi) "AI đang phân tích..." else "AI Analyzing...", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Icon(Icons.Default.AutoAwesome, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (isVi) "BẮT ĐẦU PHÂN TÍCH" else "START ANALYSIS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }

    LaunchedEffect(analyzing) {
        if (!analyzing) return@LaunchedEffect
        try {
            delay(1000)
            val scan = appState.analyzeScan(selectedImageUri)
            showToast(context, if (isVi) "Phân tích thành công" else "Analysis success")
            navController.navigate(Routes.skinReport(scan.id))
        } catch (e: Exception) {
            showToast(context, e.message ?: (if (isVi) "Lỗi kết nối hệ thống" else "System connection error"))
        } finally {
            analyzing = false
        }
    }
}

@Composable
fun ProfileScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    val isVi = appState.language == "vi"
    val userName = appState.backendUserName ?: appState.firebaseUser?.displayName ?: "User"
    val userEmail = appState.backendUserEmail ?: appState.firebaseUser?.email ?: ""
    var showEditProfile by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    ScreenColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        GradientHeader(
            title = if (isVi) "Tài khoản" else "Profile",
            subtitle = userEmail,
            colors = listOf(Teal, Cyan),
            action = {
                IconButton(onClick = { showToast(context, if (isVi) "Đã sao chép" else "Copied") }) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                }
            },
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ModernCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Teal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Teal, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(userName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text(userEmail.ifBlank { "No email" }, color = Color.Gray, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    onClick = { showEditProfile = true },
                    shape = CircleShape,
                    color = Color(0xFFF3F4F6),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, null, tint = Teal, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                ProfileStat(if (isVi) "Lượt quét" else "Scans", appState.scanHistory.size.toString())
                ProfileStat(if (isVi) "Điểm da" else "Score", "${appState.scanHistory.lastOrNull()?.score ?: 0}%")
                ProfileStat(if (isVi) "Duy trì" else "Streak", "7")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        SectionTitle(if (isVi) "Tính năng chính" else "Core Features")
        Spacer(modifier = Modifier.height(16.dp))
        
        val menuItems = listOf(
            Triple(if (isVi) "Theo dõi tiến trình" else "Progress Tracking", Icons.AutoMirrored.Filled.TrendingUp, Routes.Progress),
            Triple(if (isVi) "Gợi ý sản phẩm" else "Skincare Solutions", Icons.Default.ShoppingCart, Routes.Products),
            Triple(if (isVi) "Trung tâm thông báo" else "Notifications Center", Icons.Default.Notifications, Routes.Notifications),
            Triple(if (isVi) "Cài đặt hệ thống" else "App Settings", Icons.Default.Settings, Routes.Settings),
        )
        
        menuItems.forEach { item ->
            ListNavRow(item.first, item.second) { navController.navigate(item.third) }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                appState.logout()
                navController.navigate(Routes.Login) { popUpTo(navController.graph.startDestinationId) { inclusive = true } }
            }, 
            modifier = Modifier.fillMaxWidth().height(58.dp), 
            shape = AppShapes.Button,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Red)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(modifier = Modifier.width(10.dp))
            Text(if (isVi) "Đăng xuất" else "Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    if (showEditProfile) {
        var newName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text(if (isVi) "Cập nhật thông tin" else "Edit Profile") },
            text = {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    AppTextField(label = if (isVi) "Họ và tên" else "Full Name", value = newName, onValueChange = { newName = it })
                }
            },
            confirmButton = {
                Button(onClick = { 
                    scope.launch {
                        try {
                            appState.updateProfile(newName)
                            showEditProfile = false 
                            showToast(context, if (isVi) "Đã lưu" else "Saved")
                        } catch (e: Exception) {
                            showToast(context, e.message ?: "Error updating profile")
                        }
                    }
                }, shape = AppShapes.Button) { Text(if (isVi) "Lưu" else "Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) { Text(if (isVi) "Hủy" else "Cancel") }
            }
        )
    }
}

@Composable
fun HistoryScreen(appState: DermascanAppState, navController: NavHostController) {
    val isVi = appState.language == "vi"
    var filter by rememberSaveable { mutableStateOf("all") }
    var queryText by rememberSaveable { mutableStateOf("") }
    
    val scans = remember(appState.scanHistory.size, filter, queryText) {
        appState.scanHistory.sortedByDescending { it.dateMillis }.filter { scan ->
            val matchFilter = when (filter) {
                "week" -> scan.dateMillis >= System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                "month" -> monthOf(scan.dateMillis) == nowMonth()
                else -> true
            }
            val matchQuery = queryText.isBlank() || scan.type.contains(queryText, ignoreCase = true)
            matchFilter && matchQuery
        }
    }

    ScreenColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        BackHeader(
            title = if (isVi) "Lịch sử phân tích" else "Scan History", 
            subtitle = if (isVi) "${appState.scanHistory.size} lượt quét" else "${appState.scanHistory.size} total entries", 
            colors = listOf(Teal, Cyan),
            onBack = { navController.popBackStack() },
            backIcon = Icons.AutoMirrored.Filled.ArrowBack,
            actions = {
                IconButton(onClick = { navController.navigate(Routes.Compare) }) { 
                    Icon(Icons.Default.Compare, null, tint = Color.White)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AppTextField(
            label = if (isVi) "Tìm kiếm lượt quét..." else "Search history...", 
            value = queryText, 
            onValueChange = { queryText = it }, 
            leading = { Icon(Icons.Default.Search, null, tint = Color.Gray) }
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        val filterOptions = listOf(
            "all" to (if (isVi) "Tất cả" else "All"), 
            "week" to (if (isVi) "Tuần này" else "This Week"), 
            "month" to (if (isVi) "Tháng này" else "This Month")
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filterOptions) { item ->
                FilterChip(
                    selected = (filter == item.first), 
                    onClick = { filter = item.first }, 
                    label = { Text(item.second) },
                    shape = AppShapes.Small,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Teal, 
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color.Gray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (filter == item.first) Teal else Color.LightGray.copy(alpha = 0.5f),
                        selectedBorderColor = Teal,
                        enabled = true,
                        selected = filter == item.first
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (appState.loadingScanHistory) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { SkeletonBox(Modifier.fillMaxWidth().height(92.dp).clip(AppShapes.Card)) }
            }
        } else if (scans.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History, 
                title = if (isVi) "Lịch sử trống" else "No Results", 
                subtitle = if (isVi) "Chúng tôi không tìm thấy kết quả nào phù hợp với bộ lọc của bạn." else "No scan records match your current filter or search query.", 
                actionLabel = if (isVi) "Quét mới" else "Start New Scan"
            ) {
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
fun ChatbotScreen(appState: DermascanAppState) {
    val isVi = appState.language == "vi"
    val messages = remember {
        mutableStateListOf(ChatMessage("1", if (isVi) "Xin chào! Tôi là trợ lý AI chuyên gia về da liễu. Bạn cần tư vấn gì hôm nay?" else "Hello. I'm your AI Skincare Expert. How can I help you today?", false, "now"))
    }
    var inputText by rememberSaveable { mutableStateOf("") }
    var typing by rememberSaveable { mutableStateOf(false) }
    var chatId by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val history = appState.loadLatestChatMessages()
            if (history != null && history.second.isNotEmpty()) {
                chatId = history.first
                messages.clear()
                messages.addAll(history.second)
            }
        } catch (e: Exception) {
            // Ignore error, keep initial greeting
        }
    }

    fun send(message: String) {
        if (message.isBlank()) return
        messages.add(ChatMessage(System.currentTimeMillis().toString(), message, true, "now"))
        inputText = ""
        typing = true
        scope.launch {
            runCatching { appState.sendChatMessage(message, chatId) }.onSuccess { (nextChatId, reply) ->
                chatId = nextChatId
                messages.add(reply)
            }.onFailure { error ->
                messages.add(ChatMessage(id = System.currentTimeMillis().toString(), text = error.message ?: (if (isVi) "Lỗi kết nối AI." else "AI Connection failed."), fromUser = false, timestampLabel = "error"))
            }
            typing = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(LightBackground)) {
        Box(modifier = Modifier.padding(20.dp)) {
            GradientHeader(
                title = if (isVi) "Tư vấn AI Expert" else "AI Skincare Expert", 
                subtitle = if (isVi) "Sẵn sàng hỗ trợ 24/7" else "Available 24/7 for your skin", 
                colors = listOf(Purple, Pink)
            )
        }
        
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            messages.forEach { message ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 20.dp, 
                            topEnd = 20.dp, 
                            bottomStart = if (message.fromUser) 20.dp else 4.dp, 
                            bottomEnd = if (message.fromUser) 4.dp else 20.dp
                        ), 
                        color = if (message.fromUser) Teal else Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = message.text, color = if (message.fromUser) Color.White else Color(0xFF1F2937), fontSize = 15.sp, lineHeight = 22.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = message.timestampLabel, color = if (message.fromUser) Color.White.copy(alpha = 0.7f) else Color.Gray, fontSize = 11.sp, textAlign = if (message.fromUser) TextAlign.End else TextAlign.Start, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            if (typing) {
                Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Purple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isVi) "AI đang suy nghĩ..." else "Expert is thinking...", color = Purple, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth().padding(20.dp).padding(bottom = 16.dp),
            shape = AppShapes.Input,
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Teal, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(if (isVi) "Nhập câu hỏi..." else "Ask about your skin...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    maxLines = 3
                )
                IconButton(onClick = { send(inputText) }, enabled = inputText.isNotBlank() && !typing) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = if (inputText.isNotBlank()) Teal else Color.LightGray)
                }
            }
        }
    }
}
