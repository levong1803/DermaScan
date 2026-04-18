package com.example.dermascan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dermascan.data.DermascanAppState
import com.example.dermascan.ui.navigation.AppBottomBar
import com.example.dermascan.ui.navigation.AppNavGraph
import com.example.dermascan.ui.navigation.bottomRoutes
import com.example.dermascan.ui.theme.DermascanTheme

@Composable
fun DermaScanApp() {
    val context = LocalContext.current
    val appState = remember { DermascanAppState(context.applicationContext) }
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    DermascanTheme(darkMode = appState.darkMode) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (currentRoute in bottomRoutes) {
                    AppBottomBar(navController, currentRoute.orEmpty())
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavGraph(navController = navController, appState = appState)
            }
        }
    }
}
