package com.example.dermascan.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dermascan.data.DermascanAppState
import com.example.dermascan.ui.components.AppTextField
import com.example.dermascan.ui.components.FormCard
import com.example.dermascan.ui.components.GradientScreen
import com.example.dermascan.ui.navigation.Routes
import com.example.dermascan.ui.theme.Blue
import com.example.dermascan.ui.theme.Teal
import com.example.dermascan.util.showToast
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }

    GradientScreen(colors = listOf(Teal, Blue)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome Back", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sign in to continue your skin care journey", color = Color.White.copy(alpha = 0.85f))
            Spacer(modifier = Modifier.height(20.dp))
            FormCard {
                Text("Tài khoản demo", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Email: demo@dermascan.com", color = Color.Gray)
                Text("Password: demo123", color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { email = "demo@dermascan.com"; password = "demo123" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Use Demo Account")
                }
                Spacer(modifier = Modifier.height(16.dp))
                AppTextField("Email", email, { email = it }, leading = { Icon(Icons.Default.Email, null) })
                Spacer(modifier = Modifier.height(14.dp))
                AppTextField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    leading = { Icon(Icons.Default.Lock, null) },
                    trailing = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate(Routes.ForgotPassword) }, modifier = Modifier.align(Alignment.End)) {
                    Text("Forgot password?")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { loading = true }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Sign In")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = { navController.navigate(Routes.Register) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Create account")
                }
            }
        }
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        delay(900)
        val error = appState.login(email.trim(), password)
        loading = false
        if (error == null) {
            showToast(context, "Welcome to DermaScan")
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Login) { inclusive = true }
            }
        } else {
            showToast(context, error)
        }
    }
}

@Composable
fun RegisterScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }

    GradientScreen(colors = listOf(Teal, Blue)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Create Account", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Join DermaScan to start your skin care journey", color = Color.White.copy(alpha = 0.85f))
            Spacer(modifier = Modifier.height(18.dp))
            FormCard {
                AppTextField("Full Name", name, { name = it }, leading = { Icon(Icons.Default.Person, null) })
                Spacer(modifier = Modifier.height(14.dp))
                AppTextField("Email", email, { email = it }, leading = { Icon(Icons.Default.Email, null) })
                Spacer(modifier = Modifier.height(14.dp))
                AppTextField(
                    "Password",
                    password,
                    { password = it },
                    leading = { Icon(Icons.Default.Lock, null) },
                    trailing = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                )
                Spacer(modifier = Modifier.height(14.dp))
                AppTextField(
                    "Confirm Password",
                    confirmPassword,
                    { confirmPassword = it },
                    leading = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(onClick = { loading = true }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Create Account")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Back to sign in")
                }
            }
        }
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        when {
            password != confirmPassword -> {
                showToast(context, "Passwords do not match")
                loading = false
            }
            password.length < 6 -> {
                showToast(context, "Password must be at least 6 characters")
                loading = false
            }
            else -> {
                delay(900)
                val error = appState.register(name.trim(), email.trim(), password)
                loading = false
                if (error == null) {
                    showToast(context, "Account created. Verify your email.")
                    navController.navigate(Routes.Verify)
                } else {
                    showToast(context, error)
                }
            }
        }
    }
}

@Composable
fun VerifyScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    var otp by rememberSaveable { mutableStateOf("") }
    var loading by rememberSaveable { mutableStateOf(false) }

    GradientScreen(colors = listOf(Teal, Blue)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(84.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Email, contentDescription = null, tint = Teal, modifier = Modifier.size(42.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Verify Email", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Enter the 6-digit code sent to your email", color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
            Text("Use 123456 for demo", color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(18.dp))
            FormCard {
                AppTextField("OTP Code", otp, { if (it.length <= 6) otp = it }, leading = { Icon(Icons.Default.Lock, null) })
                Spacer(modifier = Modifier.height(18.dp))
                Button(onClick = { loading = true }, enabled = otp.length == 6, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Verify Email")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(onClick = { showToast(context, "OTP sent again") }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Resend")
                }
            }
        }
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        delay(800)
        val error = appState.verifyOtp(otp)
        loading = false
        if (error == null) {
            showToast(context, "Email verified successfully")
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Login) { inclusive = true }
            }
        } else {
            showToast(context, error)
        }
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavHostController) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }

    GradientScreen(colors = listOf(Teal, Blue)) {
        TextButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back to Login", color = Color.White)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text("Forgot Password?", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter your email and we'll send you a reset link", color = Color.White.copy(alpha = 0.85f))
        Spacer(modifier = Modifier.height(18.dp))
        FormCard {
            if (!sent) {
                AppTextField("Email", email, { email = it }, leading = { Icon(Icons.Default.Email, null) })
                Spacer(modifier = Modifier.height(18.dp))
                Button(onClick = { loading = true }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Send Reset Link")
                    }
                }
            } else {
                Icon(Icons.Default.Email, contentDescription = null, tint = Teal, modifier = Modifier.size(52.dp).align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Check your email", fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
                Text("We've sent a password reset link to $email", textAlign = TextAlign.Center, color = Color.Gray)
            }
        }
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        delay(900)
        loading = false
        sent = true
        showToast(context, "Reset link sent")
    }
}
