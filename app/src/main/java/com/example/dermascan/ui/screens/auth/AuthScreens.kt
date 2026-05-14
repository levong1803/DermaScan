package com.example.dermascan.ui.screens.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dermascan.data.DermascanAppState
import com.example.dermascan.ui.components.AppShapes
import com.example.dermascan.ui.components.AppTextField
import com.example.dermascan.ui.components.FormCard
import com.example.dermascan.ui.components.GradientScreen
import com.example.dermascan.ui.navigation.Routes
import com.example.dermascan.ui.theme.Blue
import com.example.dermascan.ui.theme.Teal
import com.example.dermascan.util.GoogleAuthHelper
import com.example.dermascan.util.showToast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }

    val googleAuthHelper = remember { GoogleAuthHelper(context) }
    val callbackManager = remember { CallbackManager.Factory.create() }
    val loginManager = remember { LoginManager.getInstance() }

    val facebookLauncher = rememberLauncherForActivityResult(
        loginManager.createLogInActivityResultContract(callbackManager)
    ) { }

    DisposableEffect(Unit) {
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                scope.launch {
                    try {
                        appState.getAuthRepo().signInWithFacebook(result.accessToken.token)
                        navController.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } }
                    } catch (e: Exception) {
                        showToast(context, "Lỗi Facebook: ${e.localizedMessage}")
                    }
                }
            }
            override fun onCancel() { showToast(context, "Đã hủy FB") }
            override fun onError(error: FacebookException) { showToast(context, "Lỗi FB: ${error.message}") }
        })
        onDispose { loginManager.unregisterCallback(callbackManager) }
    }

    GradientScreen(colors = listOf(Teal, Blue)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text("Welcome Back", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
            Text("Sign in to continue your skincare journey", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(48.dp))
            
            FormCard {
                AppTextField("Email Address", email, { email = it }, leading = { Icon(Icons.Default.Email, null, tint = Teal) })
                Spacer(modifier = Modifier.height(16.dp))
                AppTextField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    leading = { Icon(Icons.Default.Lock, null, tint = Teal) },
                    trailing = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color.Gray)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { navController.navigate(Routes.ForgotPassword) }) {
                        Text("Forgot password?", color = Teal, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { 
                        if (email.isNotBlank() && password.isNotBlank()) loading = true 
                        else showToast(context, "Please enter email and password")
                    }, 
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = AppShapes.Button,
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    else Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                SocialButtons(
                    onGoogleClick = {
                        scope.launch {
                            try {
                                val idToken = googleAuthHelper.signIn()
                                if (idToken != null) {
                                    appState.getAuthRepo().signInWithGoogle(idToken)
                                    navController.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } }
                                }
                            } catch (e: Exception) {
                                showToast(context, "Google Error: ${e.localizedMessage}")
                            }
                        }
                    },
                    onFacebookClick = { facebookLauncher.launch(listOf("email", "public_profile")) }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Don't have an account?", color = Color.Gray)
                    TextButton(onClick = { navController.navigate(Routes.Register) }) {
                        Text("Create one", color = Teal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        try {
            appState.getAuthRepo().signInWithEmail(email.trim(), password)
            navController.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } }
        } catch (e: Exception) {
            showToast(context, e.localizedMessage ?: "Login failed")
        } finally {
            loading = false
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
    var loading by rememberSaveable { mutableStateOf(false) }

    GradientScreen(colors = listOf(Teal, Blue)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text("Create Account", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text("Join our skincare community", color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(40.dp))
            
            FormCard {
                AppTextField("Full Name", name, { name = it }, leading = { Icon(Icons.Default.Person, null, tint = Teal) })
                Spacer(modifier = Modifier.height(12.dp))
                AppTextField("Email Address", email, { email = it }, leading = { Icon(Icons.Default.Email, null, tint = Teal) })
                Spacer(modifier = Modifier.height(12.dp))
                AppTextField("Password", password, { password = it }, leading = { Icon(Icons.Default.Lock, null, tint = Teal) }, visualTransformation = PasswordVisualTransformation())
                Spacer(modifier = Modifier.height(12.dp))
                AppTextField("Confirm Password", confirmPassword, { confirmPassword = it }, leading = { Icon(Icons.Default.Lock, null, tint = Teal) }, visualTransformation = PasswordVisualTransformation())
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { 
                        if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank()) loading = true
                        else showToast(context, "Please fill all fields")
                    }, 
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = AppShapes.Button,
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    else Text("Register", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Already have an account? Sign In", color = Teal, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        if (password != confirmPassword) {
            showToast(context, "Passwords do not match")
            loading = false
            return@LaunchedEffect
        }
        try {
            appState.getAuthRepo().signUpWithEmail(name, email.trim(), password)
            showToast(context, "Success! Please check your email for verification.")
            navController.popBackStack()
        } catch (e: Exception) {
            showToast(context, "Registration error: ${e.localizedMessage}")
        } finally {
            loading = false
        }
    }
}

@Composable
fun ForgotPasswordScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf("") }
    var loading by rememberSaveable { mutableStateOf(false) }

    GradientScreen(colors = listOf(Teal, Blue)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text("Reset Password", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            FormCard {
                Box(modifier = Modifier.size(80.dp).clip(AppShapes.Card).background(Teal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lock, null, tint = Teal, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Recover your password", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter your email address and we'll send you a link to reset your password.", fontSize = 15.sp, color = Color.Gray, textAlign = TextAlign.Center)
                
                Spacer(modifier = Modifier.height(32.dp))
                AppTextField("Email Address", email, { email = it }, leading = { Icon(Icons.Default.Email, null, tint = Teal) })
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (email.isBlank()) { showToast(context, "Please enter email"); return@Button }
                        loading = true
                        scope.launch {
                            try {
                                appState.getAuthRepo().sendPasswordReset(email.trim())
                                showToast(context, "Reset link sent! Please check your inbox.")
                                navController.popBackStack()
                            } catch (e: Exception) { 
                                showToast(context, "Error: ${e.localizedMessage}")
                            } finally { loading = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = AppShapes.Button,
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    else Text("Send Reset Link", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun SocialButtons(onGoogleClick: () -> Unit, onFacebookClick: () -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
            Text("  OR  ", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onGoogleClick, 
            modifier = Modifier.fillMaxWidth().height(56.dp), 
            shape = AppShapes.Button,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onFacebookClick, 
            modifier = Modifier.fillMaxWidth().height(56.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)), 
            shape = AppShapes.Button
        ) {
            Text("Continue with Facebook", color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun VerifyScreen(appState: DermascanAppState, navController: NavHostController) {}
