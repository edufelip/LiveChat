package com.project.livechat.ui

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.project.livechat.ui.navigation.builder.HomeScreen
import com.project.livechat.ui.navigation.builder.contactsRoute
import com.project.livechat.ui.navigation.builder.homeRoute
import com.project.livechat.ui.navigation.builder.onBoardingRoute
import com.project.livechat.ui.theme.LiveChatTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            LiveChatTheme {
                UpdateSystemBars(window = window)
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = HomeScreen,
                ) {
                    onBoardingRoute(
                        navHostController = navController
                    )
                    homeRoute(
                        navHostController = navController,
                        onBackPressedDispatcher = onBackPressedDispatcher
                    )
                    contactsRoute(
                        navHostController = navController,
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateSystemBars(window: Window) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val surfaceColor = MaterialTheme.colorScheme.surface
    SideEffect {
        @Suppress("DEPRECATION")
        window.statusBarColor = surfaceColor.toArgb()
        WindowInsetsControllerCompat(window, view)
            .isAppearanceLightStatusBars = surfaceColor.luminance() > 0.5f
    }
}
