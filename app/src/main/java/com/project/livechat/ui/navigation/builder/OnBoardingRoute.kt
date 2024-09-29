package com.project.livechat.ui.navigation.builder

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.project.livechat.ui.screens.onboarding.OnBoardingScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.onBoardingRoute(
    navHostController: NavHostController,
) {
    composable<OnboardingScreen> {
//        val args = it.toRoute<OnboardingScreen>()
        OnBoardingScreen(
            navHostController = navHostController,
        )
    }
}

@Serializable
object OnboardingScreen