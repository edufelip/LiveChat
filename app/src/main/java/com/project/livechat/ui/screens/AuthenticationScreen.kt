package com.project.livechat.ui.screens

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.project.livechat.ui.navigation.builder.OnboardingScreen
import com.project.livechat.ui.screens.intro.IntroScreen

@Composable
fun AuthenticationScreen(
    navHostController: NavHostController,
    backPressedDispatcher: OnBackPressedDispatcher
) {
    IntroScreen(
        onGetStarted = {
            navHostController.navigate(OnboardingScreen) {
                launchSingleTop = true
            }
        }
    )
}
