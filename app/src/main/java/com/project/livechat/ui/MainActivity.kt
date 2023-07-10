package com.project.livechat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.MaterialTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.project.livechat.ui.navigation.Routes
import com.project.livechat.ui.navigation.builder.contactsRoute
import com.project.livechat.ui.navigation.builder.homeRoute
import com.project.livechat.ui.navigation.builder.onBoardingRoute
import com.project.livechat.ui.theme.LiveChatTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LiveChatTheme {
                val systemUiController = rememberSystemUiController()
                val navController = rememberAnimatedNavController()
                systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
                AnimatedNavHost(
                    navController = navController,
                    startDestination = if (firebaseAuth.currentUser != null)
                        Routes.HomeRoute.route
                    else
                        Routes.OnBoardingRoute.route,
                    builder = {
                        onBoardingRoute(
                            navHostController = navController,
                        )
                        homeRoute(
                            navHostController = navController,
                            onBackPressedDispatcher = onBackPressedDispatcher
                        )
                        contactsRoute(
                            navHostController = navController,
                            onBackPressedDispatcher = onBackPressedDispatcher,
                        )
                    }
                )
            }
        }
    }
}