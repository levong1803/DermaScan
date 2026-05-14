package com.example.dermascan.ui.screens.startup

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
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
import androidx.navigation.NavHostController
import com.example.dermascan.data.DermascanAppState
import com.example.dermascan.ui.components.*
import com.example.dermascan.ui.navigation.Routes
import com.example.dermascan.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(appState: DermascanAppState, navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(2000)
        val target = when {
            !appState.hasSeenOnboarding -> Routes.Onboarding
            !appState.cameraGranted -> Routes.Permissions
            !appState.isAuthenticated -> Routes.Login
            else -> Routes.Home
        }
        navController.navigate(target) {
            popUpTo(Routes.Splash) { inclusive = true }
        }
    }

    GradientScreen(colors = listOf(Teal, Cyan, Blue)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.size(90.dp), shape = CircleShape, color = Color.White) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Teal, modifier = Modifier.size(48.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("DermaScan", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
            Text("AI Skin Intelligence", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun OnboardingScreen(appState: DermascanAppState, navController: NavHostController) {
    val slides = listOf(
        Triple("AI Skin Scan", "Advanced algorithms to detect skin conditions in seconds.", listOf(Teal, Cyan)),
        Triple("Track Changes", "Visual progress tracking with professional charts.", listOf(Cyan, Blue)),
        Triple("Expert Support", "AI-powered chat for immediate skincare guidance.", listOf(Blue, Purple)),
        Triple("Data Privacy", "Your medical data is encrypted and stored securely.", listOf(Purple, Pink)),
    )
    var index by rememberSaveable { mutableIntStateOf(0) }
    val slide = slides[index]

    GradientScreen(colors = slide.third) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { appState.finishOnboarding(); navController.navigate(Routes.Permissions) { popUpTo(Routes.Onboarding) { inclusive = true } } }) {
                    Text("Skip", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            AnimatedContent(
                targetState = index,
                transitionSpec = { fadeIn() + slideInHorizontally { it } togetherWith fadeOut() + slideOutHorizontally { -it } },
                label = "OnboardingContent"
            ) { targetIndex ->
                val currentSlide = slides[targetIndex]
                val currentIcon = when (targetIndex) {
                    0 -> Icons.Default.CameraAlt
                    1 -> Icons.AutoMirrored.Filled.TrendingUp
                    2 -> Icons.AutoMirrored.Filled.Chat
                    else -> Icons.Default.Security
                }
                
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(160.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Surface(modifier = Modifier.size(110.dp), shape = CircleShape, color = Color.White) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(currentIcon, contentDescription = null, tint = currentSlide.third.first(), modifier = Modifier.size(50.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(currentSlide.first, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(currentSlide.second, color = Color.White.copy(alpha = 0.9f), fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1.2f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                slides.forEachIndexed { i, _ ->
                    Box(modifier = Modifier.height(6.dp).width(if (i == index) 24.dp else 6.dp).clip(CircleShape).background(Color.White.copy(alpha = if (i == index) 1f else 0.4f)))
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (index < slides.lastIndex) index++
                    else { appState.finishOnboarding(); navController.navigate(Routes.Permissions) { popUpTo(Routes.Onboarding) { inclusive = true } } }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = slide.third.first()),
                shape = AppShapes.Button,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
            ) {
                Text(if (index == slides.lastIndex) "Get Started" else "Continue", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun PermissionScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        appState.setPermission("camera", isGranted)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        appState.setPermission("notifications", isGranted)
    }

    // Tự động cập nhật trạng thái nếu người dùng đã cấp quyền trước đó
    LaunchedEffect(Unit) {
        val cam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        appState.setPermission("camera", cam)
    }

    ScreenColumn {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Smart Access", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
        Text("We need your permission to provide accurate AI diagnostics.", color = Color.Gray, fontSize = 16.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        PermissionItem(
            "camera", 
            "Camera", 
            "Essential for high-resolution skin scanning.", 
            appState.cameraGranted
        ) { 
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA) 
        }
        
        PermissionItem(
            "notifications", 
            "Notifications", 
            "Receive analysis alerts and skincare routines.", 
            appState.notificationsEnabled
        ) { 
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                appState.setPermission("notifications", true)
            }
        }
        
        PermissionItem(
            "location", 
            "Location", 
            "Connect with local dermatologists nearby.", 
            appState.locationGranted
        ) { 
            appState.setPermission("location", true) 
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { navController.navigate(Routes.Login) { popUpTo(Routes.Permissions) { inclusive = true } } },
            enabled = appState.cameraGranted,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = AppShapes.Button
        ) {
            Text("Go to Sign In", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PermissionItem(id: String, title: String, desc: String, granted: Boolean, onGrant: () -> Unit) {
    ModernCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icon = when(id) {
                "camera" -> Icons.Default.CameraAlt
                "notifications" -> Icons.Default.Notifications
                else -> Icons.Default.Security
            }
            Box(modifier = Modifier.size(52.dp).clip(AppShapes.Small).background(Teal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Teal)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(desc, color = Color.Gray, fontSize = 14.sp)
            }
            if (granted) {
                Icon(Icons.Default.CheckCircle, null, tint = Green, modifier = Modifier.size(28.dp))
            } else {
                TextButton(onClick = onGrant) { Text("Allow", fontWeight = FontWeight.Bold) }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}
