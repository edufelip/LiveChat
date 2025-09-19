package com.project.livechat.ui.navigation.builder

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.project.livechat.ui.screens.onboarding.OnBoardingScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.onBoardingRoute(
    navHostController: NavHostController,
) {
    composable<OnboardingScreen> { backStackEntry ->
        OnBoardingScreen(
            navHostController = navHostController,
            viewModelStoreOwner = backStackEntry
        )
    }
}

@Serializable
object OnboardingScreen
