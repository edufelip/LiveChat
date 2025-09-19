package com.project.livechat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.livechat.ui.navigation.builder.HomeScreen
import com.project.livechat.ui.navigation.builder.contactsRoute
import com.project.livechat.ui.navigation.builder.homeRoute
import com.project.livechat.ui.navigation.builder.onBoardingRoute
import com.project.livechat.ui.theme.LiveChatTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LiveChatTheme {
                val systemUiController = rememberSystemUiController()
                val navController = rememberNavController()
                systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
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
