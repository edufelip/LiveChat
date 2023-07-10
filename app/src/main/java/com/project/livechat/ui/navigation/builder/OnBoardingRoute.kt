package com.project.livechat.ui.navigation.builder

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.project.livechat.ui.navigation.Routes
import com.project.livechat.ui.screens.onboarding.OnBoardingScreen
import com.project.livechat.ui.utils.exitTransition
import com.project.livechat.ui.utils.popEnterTransition

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.onBoardingRoute(
    navHostController: NavHostController,
) {
    composable(
        route = Routes.OnBoardingRoute.route,
        popEnterTransition = { popEnterTransition },
        exitTransition = { exitTransition },
    ) {
        OnBoardingScreen(
            navHostController = navHostController,
        )
    }
}