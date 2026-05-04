package com.example.dermascan.ui.screens.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
        Spacer(modifier = Modifier.height(40.dp))
        Text("Welcome Back", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text("Sign in to continue", color = Color.White.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(30.dp))
        
        FormCard {
            AppTextField("Email", email, { email = it }, leading = { Icon(Icons.Default.Email, null) })
            Spacer(modifier = Modifier.height(12.dp))
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
            
            TextButton(
                onClick = { 
                    Log.d("AuthNav", "Navigating to ForgotPassword")
                    navController.navigate(Routes.ForgotPassword) 
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot password?")
            }

            Button(
                onClick = { 
                    if (email.isNotBlank() && password.isNotBlank()) loading = true 
                    else showToast(context, "Nhập đầy đủ email và mật khẩu")
                }, 
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Sign In")
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
                            showToast(context, "Lỗi Google: ${e.localizedMessage}")
                        }
                    }
                },
                onFacebookClick = { facebookLauncher.launch(listOf("email", "public_profile")) }
            )

            TextButton(onClick = { navController.navigate(Routes.Register) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Don't have an account? Create one")
            }
        }
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        try {
            appState.getAuthRepo().signInWithEmail(email.trim(), password)
            navController.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } }
        } catch (e: Exception) {
            showToast(context, e.localizedMessage ?: "Đăng nhập thất bại")
        } finally {
            loading = false
        }
    }
}

@Composable
fun RegisterScreen(appState: DermascanAppState, navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var loading by rememberSaveable { mutableStateOf(false) }

    GradientScreen(colors = listOf(Teal, Blue)) {
        Spacer(modifier = Modifier.height(30.dp))
        Text("Create Account", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        
        FormCard {
            AppTextField("Name", name, { name = it }, leading = { Icon(Icons.Default.Person, null) })
            Spacer(modifier = Modifier.height(10.dp))
            AppTextField("Email", email, { email = it }, leading = { Icon(Icons.Default.Email, null) })
            Spacer(modifier = Modifier.height(10.dp))
            AppTextField("Password", password, { password = it }, leading = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(10.dp))
            AppTextField("Confirm", confirmPassword, { confirmPassword = it }, leading = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation())
            
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { 
                    if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank()) loading = true
                    else showToast(context, "Vui lòng điền đủ thông tin")
                }, 
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Register")
            }

            TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Back to Sign In")
            }
        }
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        if (password != confirmPassword) {
            showToast(context, "Mật khẩu không khớp")
            loading = false
            return@LaunchedEffect
        }
        try {
            appState.getAuthRepo().signUpWithEmail(name, email.trim(), password)
            showToast(context, "Đăng ký thành công! Vui lòng kiểm tra email để xác thực.")
            navController.popBackStack()
        } catch (e: Exception) {
            showToast(context, "Lỗi đăng ký: ${e.localizedMessage}")
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
        Spacer(modifier = Modifier.height(40.dp))
        FormCard {
            Text("Enter your email to reset password", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            AppTextField("Email Address", email, { email = it }, leading = { Icon(Icons.Default.Email, null) })
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (email.isBlank()) { showToast(context, "Vui lòng nhập email"); return@Button }
                    loading = true
                    scope.launch {
                        try {
                            appState.getAuthRepo().sendPasswordReset(email.trim())
                            showToast(context, "Đã gửi link! Kiểm tra email của bạn.")
                            navController.popBackStack()
                        } catch (e: Exception) { 
                            showToast(context, "Lỗi: ${e.localizedMessage}")
                        } finally { loading = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Send Reset Link")
            }
        }
    }
}

@Composable
fun SocialButtons(onGoogleClick: () -> Unit, onFacebookClick: () -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(" OR ", color = Color.Gray, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onGoogleClick, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) {
            Text("Continue with Google")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onFacebookClick, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)), shape = RoundedCornerShape(16.dp)) {
            Text("Continue with Facebook", color = Color.White)
        }
    }
}

@Composable
fun VerifyScreen(appState: DermascanAppState, navController: NavHostController) {}
