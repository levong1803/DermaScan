package com.example.dermascan.ui.screens.feature

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dermascan.data.DermascanAppState
import com.example.dermascan.ui.components.*
import com.example.dermascan.ui.navigation.Routes
import com.example.dermascan.ui.theme.*
import com.example.dermascan.util.*
import kotlin.math.max
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showLanguageDialog by remember { mutableStateOf(false) }
    val isVi = appState.language == "vi"

    ScreenColumn {
        BackHeader(
            title = if (isVi) "Cài đặt" else "Settings",
            colors = listOf(Teal, Cyan),
            onBack = { navController.popBackStack() },
            backIcon = Icons.AutoMirrored.Filled.ArrowBack
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        SettingsGroup(if (isVi) "Cá nhân hóa" else "Preferences") {
            SettingToggleRow(
                if (isVi) "Thông báo đẩy" else "Push Notifications", 
                Icons.Default.NotificationsActive, 
                appState.notificationsEnabled
            ) {
                appState.toggleNotificationsEnabled(it)
                performHapticFeedback(context)
            }
            SettingToggleRow(
                if (isVi) "Chế độ tối" else "Dark Mode", 
                Icons.Default.DarkMode, 
                appState.darkMode
            ) {
                appState.toggleDarkMode(it)
            }
            SettingStaticRow(
                title = if (isVi) "Ngôn ngữ ứng dụng" else "App Language", 
                icon = Icons.Default.Language, 
                trailing = if (isVi) "Tiếng Việt" else "English",
                onClick = { showLanguageDialog = true }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        SettingsGroup(if (isVi) "Bảo mật & Dữ liệu" else "Security & Data") {
            SettingStaticRow(
                title = if (isVi) "Đổi mật khẩu" else "Change Password", 
                icon = Icons.Default.LockReset,
                onClick = {
                    val email = appState.firebaseUser?.email
                    if (email != null) {
                        scope.launch {
                            try {
                                appState.getAuthRepo().sendPasswordReset(email)
                                showToast(context, if (isVi) "Email đã gửi" else "Email sent")
                            } catch (e: Exception) {
                                showToast(context, e.message ?: "Error")
                            }
                        }
                    }
                }
            )
            SettingStaticRow(
                title = if (isVi) "Chính sách bảo mật" else "Privacy Policy", 
                icon = Icons.Default.Security,
                onClick = { navController.navigate(Routes.PrivacyPolicy) }
            )
            SettingStaticRow(
                title = if (isVi) "Xóa bộ nhớ đệm" else "Clear Cache", 
                icon = Icons.Default.DeleteOutline,
                onClick = { 
                    performHapticFeedback(context)
                    showToast(context, if (isVi) "Đã dọn dẹp" else "Cleaned") 
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        SettingsGroup(if (isVi) "Thông tin" else "Information") {
            SettingStaticRow(if (isVi) "Liên hệ hỗ trợ" else "Support Center", Icons.AutoMirrored.Filled.HelpOutline)
            SettingStaticRow(if (isVi) "Phiên bản" else "Version", Icons.Default.Info, "2.0.4 Premium")
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(if (isVi) "Chọn ngôn ngữ" else "Select Language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    LanguageOption("English", !isVi) { appState.setAppLanguage("en"); showLanguageDialog = false }
                    LanguageOption("Tiếng Việt", isVi) { appState.setAppLanguage("vi"); showLanguageDialog = false }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(if (isVi) "Đóng" else "Close") }
            },
            shape = AppShapes.Card
        )
    }
}

@Composable
fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(AppShapes.Small).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Teal))
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun SkinReportScreen(appState: DermascanAppState, navController: NavHostController, scanId: String) {
    val context = LocalContext.current
    val isVi = appState.language == "vi"
    val scan = appState.scanHistory.firstOrNull { it.id == scanId }
    
    if (scan == null) {
        ScreenColumn {
            EmptyState(
                Icons.Default.ErrorOutline,
                if (isVi) "Không tìm thấy" else "Not Found",
                if (isVi) "Báo cáo này không còn tồn tại." else "This report has been removed.",
                if (isVi) "Quay lại" else "Go Back"
            ) { navController.popBackStack() }
        }
        return
    }

    LaunchedEffect(Unit) { performHapticFeedback(context) }

    ScreenColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
        BackHeader(
            title = if (isVi) "Kết quả phân tích" else "Analysis Results",
            subtitle = formatDate(scan.dateMillis),
            colors = listOf(Teal, Cyan),
            actions = {
                IconButton(onClick = { showToast(context, "Shared") }) { Icon(Icons.Default.Share, null, tint = Color.White) }
            },
            onBack = { navController.popBackStack() },
            backIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                modifier = Modifier.weight(1.2f).height(200.dp),
                shape = AppShapes.Card,
                color = Color(0xFFF3F4F6)
            ) {
                if (scan.imageUri != null) UriImage(scan.imageUri, Modifier.fillMaxSize())
                else Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Image, null, tint = Color.LightGray, modifier = Modifier.size(48.dp)) }
            }
            
            Surface(
                modifier = Modifier.weight(1f).height(200.dp),
                shape = AppShapes.Card,
                color = scoreColor(scan.score)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(if (isVi) "ĐIỂM DA" else "SKIN SCORE", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("${scan.score}", color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.ExtraBold)
                    Surface(color = Color.White.copy(alpha = 0.2f), shape = CircleShape) {
                        Text(
                            if (scan.score >= 80) (if (isVi) "Tốt" else "Good") else (if (isVi) "Cần chú ý" else "Caution"),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        InfoCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SectionTitle(if (isVi) "Chỉ số chi tiết" else "Detailed Metrics")
                Icon(Icons.Default.Info, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                val labels = listOf("Acne", "Wrinkles", "Hydration", "Texture", "Pores", "Spots")
                val values = listOf(85f, 70f, 60f, 80f, 65f, 75f)
                RadarChart(labels = labels, values = values, modifier = Modifier.size(200.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isVi) "Tuổi da" else "Skin Age", color = Color.Gray, fontSize = 13.sp)
                    Text("${scan.skinAge ?: 24}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Teal)
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isVi) "Loại da" else "Skin Type", color = Color.Gray, fontSize = 13.sp)
                    Text(if (isVi) "Hỗn hợp" else "Oily", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Teal)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        SectionTitle(if (isVi) "Tình trạng phát hiện" else "Detected Conditions")
        Spacer(modifier = Modifier.height(16.dp))
        scan.conditions.forEach { condition ->
            ModernCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(condition.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("${if (isVi) "Độ chính xác" else "Confidence"}: ${condition.confidence}%", fontSize = 13.sp, color = Color.Gray)
                    }
                    SeverityBadge(condition.severity)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        InfoCard {
            Text(if (isVi) "Lời khuyên từ AI" else "AI Recommendations", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Purple)
            Spacer(modifier = Modifier.height(16.dp))
            scan.recommendations.forEach { rec ->
                Row(modifier = Modifier.padding(bottom = 12.dp)) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Purple, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(rec, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF374151))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { navController.navigate(Routes.Products) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = AppShapes.Button,
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) {
            Icon(Icons.Default.ShoppingBag, null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(if (isVi) "Xem sản phẩm phù hợp" else "Shop Your Solutions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun RecommendationsScreen(navController: NavHostController) {
    ScreenColumn {
        BackHeader(
            title = "Personalized Advice",
            colors = listOf(Purple, Pink),
            onBack = { navController.popBackStack() },
            backIcon = Icons.AutoMirrored.Filled.ArrowBack
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        SectionTitle("Morning Routine")
        Spacer(modifier = Modifier.height(16.dp))
        RoutineCard("Cleanser", "Use a gentle foaming cleanser to remove oils.", Icons.Default.WaterDrop)
        RoutineCard("Vitamin C", "Apply antioxidant serum for protection.", Icons.Default.LightMode)
        RoutineCard("Sunscreen", "Essential SPF 50+ to prevent spots.", Icons.Default.WbSunny)
        
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("Lifestyle Tips")
        InfoCard {
            Text("• Drink at least 2L of water daily.\n• Change your pillowcase twice a week.\n• Avoid touching your face during the day.", lineHeight = 28.sp)
        }
    }
}

@Composable
fun RoutineCard(title: String, desc: String, icon: ImageVector) {
    ModernCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(AppShapes.Small).background(Purple.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Purple)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(desc, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductsScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    val isVi = appState.language == "vi"
    var filter by rememberSaveable { mutableStateOf("Recommended") }
    val scope = rememberCoroutineScope()
    val filtered = if (filter == "Recommended") appState.recommendedProducts else appState.products

    LaunchedEffect(Unit) { 
        if (appState.products.isEmpty()) appState.loadProducts() 
        if (appState.recommendedProducts.isEmpty()) filter = "All"
    }
    LaunchedEffect(filter) { 
        if (filter != "Recommended") {
            scope.launch { appState.loadProducts(if (filter == "All") null else filter) } 
        }
    }

    ScreenColumn {
        BackHeader(
            if (isVi) "Cửa hàng Giải pháp" else "Skin Solutions", 
            if (isVi) "Sản phẩm được AI đề xuất" else "AI-Recommended Products", 
            listOf(Orange, Red), 
            onBack = { navController.popBackStack() }, 
            backIcon = Icons.AutoMirrored.Filled.ArrowBack
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        val categories = listOf("Recommended", "All", "Serum", "Moisturizer", "Cleanser", "Sunscreen")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { category ->
                val label = when(category) {
                    "Recommended" -> if (isVi) "Đề xuất" else "Recommended"
                    "All" -> if (isVi) "Tất cả" else "All"
                    else -> category
                }
                FilterChip(
                    selected = filter == category, 
                    onClick = { filter = category }, 
                    label = { Text(label) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Orange, selectedLabelColor = Color.White)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (appState.loadingProducts) {
            repeat(3) { SkeletonBox(Modifier.fillMaxWidth().height(180.dp).clip(AppShapes.Card)); Spacer(Modifier.height(16.dp)) }
        } else {
            filtered.forEach { product ->
                ModernCard(onClick = { /* Detail */ }) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Surface(modifier = Modifier.size(100.dp), shape = AppShapes.Small, color = Color(0xFFF9FAFB)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Inventory, null, tint = Color.LightGray) }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.brand.uppercase(), color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(product.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                Text(" ${product.rating} (${product.reviews})", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("$${product.price}", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                        Row {
                            IconButton(onClick = { appState.toggleFavorite(product.id); performHapticFeedback(context) }) {
                                Icon(if (appState.favoriteProductIds.contains(product.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = Red)
                            }
                            Button(onClick = { showToast(context, "Added") }, shape = AppShapes.Button, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))) {
                                Text(if (isVi) "Mua" else "Buy")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen(appState: DermascanAppState, navController: NavHostController) {
    val isVi = appState.language == "vi"
    ScreenColumn {
        BackHeader(
            title = if (isVi) "Bảo mật" else "Privacy",
            colors = listOf(Teal, Blue),
            onBack = { navController.popBackStack() },
            backIcon = Icons.AutoMirrored.Filled.ArrowBack
        )
        Spacer(modifier = Modifier.height(24.dp))
        InfoCard {
            Text(if (isVi) "Cam kết bảo mật" else "Privacy Commitment", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            val text = if (isVi) "Chúng tôi sử dụng công nghệ mã hóa đầu cuối để bảo vệ hình ảnh da của bạn. Dữ liệu chỉ được dùng để phân tích AI và không bao giờ được chia sẻ với bên thứ ba." 
                       else "We use end-to-end encryption to protect your skin images. Data is only used for AI analysis and is never shared with third parties."
            Text(text, fontSize = 16.sp, lineHeight = 26.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(20.dp))
            Text("• HIPAA Compliant\n• Encrypted Storage\n• Anonymous Processing", color = Teal, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProgressScreen(appState: DermascanAppState, navController: NavHostController) {
    val scans = appState.scanHistory.sortedBy { it.dateMillis }
    val currentScore = scans.lastOrNull()?.score ?: 0
    val firstScore = scans.firstOrNull()?.score ?: currentScore
    val improvement = max(currentScore - firstScore, 0)
    val isVi = appState.language == "vi"

    ScreenColumn {
        BackHeader(if (isVi) "Tiến trình" else "My Progress", colors = listOf(Blue, Purple), onBack = { navController.popBackStack() }, backIcon = Icons.AutoMirrored.Filled.ArrowBack)
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            SmallStatCard(if (isVi) "Điểm hiện tại" else "Current", "$currentScore", "+$improvement%")
            SmallStatCard(if (isVi) "Cải thiện" else "Improvement", "$improvement%", "Total")
        }
        Spacer(modifier = Modifier.height(20.dp))
        InfoCard {
            SectionTitle(if (isVi) "Xu hướng sức khỏe" else "Health Trends")
            Spacer(modifier = Modifier.height(24.dp))
            ScoreChart(scans)
            Spacer(modifier = Modifier.height(16.dp))
            Text(if (isVi) "Dựa trên ${scans.size} lượt quét" else "Based on ${scans.size} scans", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun CompareScreen(appState: DermascanAppState, navController: NavHostController) {
    var selected by remember { mutableStateOf<List<String>>(emptyList()) }
    val scans = appState.scanHistory.sortedByDescending { it.dateMillis }
    val compareScans = selected.mapNotNull { id -> scans.firstOrNull { it.id == id } }
    val isVi = appState.language == "vi"

    ScreenColumn {
        BackHeader(
            if (isVi) "So sánh da" else "Skin Comparison", 
            if (isVi) "Chọn 2 ảnh để đối chiếu" else "Select 2 scans to compare", 
            listOf(Blue, Purple), 
            onBack = { navController.popBackStack() }, 
            backIcon = Icons.AutoMirrored.Filled.ArrowBack
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (compareScans.size == 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                compareScans.forEach { scan ->
                    Surface(modifier = Modifier.weight(1f), shape = AppShapes.Card, color = Color.White, shadowElevation = 2.dp) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(AppShapes.Small)) {
                                if (scan.imageUri != null) UriImage(scan.imageUri, Modifier.fillMaxSize())
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(formatDate(scan.dateMillis), fontSize = 12.sp, color = Color.Gray)
                            Text("${scan.score}%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = scoreColor(scan.score))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            val diff = compareScans[0].score - compareScans[1].score 
            InfoCard(modifier = Modifier.background(Blue.copy(alpha = 0.05f))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (diff >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, null, tint = Blue, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(if (diff >= 0) "+$diff% Change" else "$diff% Change", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(if (diff >= 0) "Your skin is getting better!" else "Need more care.", color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(onClick = { selected = emptyList() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = AppShapes.Button) { Text("Reset Selection") }
        } else {
            Text("${selected.size} / 2 Selected", fontWeight = FontWeight.Bold, color = Teal)
            Spacer(modifier = Modifier.height(16.dp))
            scans.forEach { scan ->
                val isSelected = selected.contains(scan.id)
                ModernCard(onClick = {
                    selected = if (isSelected) selected - scan.id else if (selected.size < 2) selected + scan.id else selected
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(60.dp), shape = AppShapes.Small, color = Color(0xFFF3F4F6)) {
                            if (scan.imageUri != null) UriImage(scan.imageUri, Modifier.fillMaxSize())
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(scan.type, fontWeight = FontWeight.Bold)
                            Text(formatDate(scan.dateMillis), color = Color.Gray, fontSize = 12.sp)
                        }
                        Checkbox(checked = isSelected, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = Teal))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun NotificationsScreen(appState: DermascanAppState, navController: NavHostController) {
    val unread = appState.notifications.count { !it.read }
    ScreenColumn {
        BackHeader(
            title = "Notifications",
            subtitle = "$unread new updates",
            colors = listOf(Teal, Cyan),
            actions = {
                if (unread > 0) IconButton(onClick = { appState.markAllNotificationsRead() }) { Icon(Icons.Default.DoneAll, null, tint = Color.White) }
            },
            onBack = { navController.popBackStack() },
            backIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (appState.notifications.isEmpty()) {
            EmptyState(Icons.Default.NotificationsNone, "No Notifications", "We will notify you here about your skin reports.", "Home") { navController.popBackStack() }
        } else {
            appState.notifications.forEach { notification ->
                ModernCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(notification.accent.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(notification.icon, null, tint = notification.accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (!notification.read) Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Teal))
                            }
                            Text(notification.message, color = Color.Gray, fontSize = 14.sp, lineHeight = 20.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(notification.time, color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private fun performHapticFeedback(context: android.content.Context) {
    val vibrator = context.getSystemService(Vibrator::class.java)
    if (vibrator != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(60)
        }
    }
}
