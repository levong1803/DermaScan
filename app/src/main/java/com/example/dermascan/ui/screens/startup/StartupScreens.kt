package com.example.dermascan.ui.screens.startup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dermascan.data.DermascanAppState
import com.example.dermascan.ui.components.GradientScreen
import com.example.dermascan.ui.components.ScreenColumn
import com.example.dermascan.ui.navigation.Routes
import com.example.dermascan.ui.theme.Blue
import com.example.dermascan.ui.theme.Cyan
import com.example.dermascan.ui.theme.Teal
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(appState: DermascanAppState, navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(2200)
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
            Box(modifier = Modifier.size(110.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Teal, modifier = Modifier.size(54.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("DermaScan", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("AI-Powered Skin Analysis", color = Color.White.copy(alpha = 0.9f), fontSize = 18.sp)
        }
    }
}

@Composable
fun OnboardingScreen(appState: DermascanAppState, navController: NavHostController) {
    val slides = listOf(
        Triple("AI Skin Analysis", "Take a photo and get instant insights about your skin condition.", listOf(Teal, Cyan)),
        Triple("Track Your Progress", "Follow score changes and compare scans across time.", listOf(Cyan, Blue)),
        Triple("AI Chat Assistant", "Ask skincare questions and get quick guidance inside the app.", listOf(Blue, com.example.dermascan.ui.theme.Purple)),
        Triple("Secure & Private", "Your sessions stay on-device in this Android Studio conversion.", listOf(com.example.dermascan.ui.theme.Purple, com.example.dermascan.ui.theme.Pink)),
    )
    var index by rememberSaveable { mutableStateOf(0) }
    val slide = slides[index]
    val icon = when (index) {
        0 -> Icons.Default.CameraAlt
        1 -> Icons.Default.TrendingUp
        2 -> Icons.Default.Chat
        else -> Icons.Default.Security
    }

    GradientScreen(colors = slide.third) {
        TextButton(
            onClick = {
                appState.finishOnboarding()
                navController.navigate(Routes.Permissions) {
                    popUpTo(Routes.Onboarding) { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Skip", color = Color.White)
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(140.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = slide.third.first(), modifier = Modifier.size(54.dp))
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(slide.first, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text(slide.second, color = Color.White.copy(alpha = 0.9f), fontSize = 18.sp, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            slides.forEachIndexed { slideIndex, _ ->
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(if (slideIndex == index) 28.dp else 8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (slideIndex == index) 1f else 0.35f)),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (index < slides.lastIndex) {
                    index += 1
                } else {
                    appState.finishOnboarding()
                    navController.navigate(Routes.Permissions) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = slide.third.first()),
            shape = RoundedCornerShape(20.dp),
        ) {
            Text(if (index == slides.lastIndex) "Get Started" else "Next", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PermissionScreen(appState: DermascanAppState, navController: NavHostController) {
    val permissions = listOf(
        Triple("camera", "Camera Access", "Required to scan and analyze your skin"),
        Triple("notifications", "Notifications", "Get reminders and skincare tips"),
        Triple("location", "Location", "Find nearby dermatologists and clinics"),
    )
    ScreenColumn {
        Text("Enable Permissions", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("We need a few permissions to match the original app flow.", color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        permissions.forEach { item ->
            val granted = when (item.first) {
                "camera" -> appState.cameraGranted
                "notifications" -> appState.notificationsGranted
                else -> appState.locationGranted
            }
            ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp), shape = RoundedCornerShape(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (item.first) {
                        "camera" -> Icons.Default.CameraAlt
                        "notifications" -> Icons.Default.Notifications
                        else -> Icons.Default.Search
                    }
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(Teal.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = Teal)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.second, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.third, color = Color.Gray, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    if (granted) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                    } else {
                        TextButton(onClick = { appState.setPermission(item.first, true) }) {
                            Text("Grant")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                navController.navigate(Routes.Login) {
                    popUpTo(Routes.Permissions) { inclusive = true }
                }
            },
            enabled = appState.cameraGranted,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text("Continue")
        }
    }
}
