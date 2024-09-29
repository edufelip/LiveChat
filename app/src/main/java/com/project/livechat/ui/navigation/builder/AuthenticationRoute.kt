package com.project.livechat.ui.navigation.builder

import androidx.activity.OnBackPressedDispatcher
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.project.livechat.ui.screens.AuthenticationScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.authenticationRoute(
    navHostController: NavHostController,
    onBackPressedDispatcher: OnBackPressedDispatcher
) {
    composable<AuthenticationScreen> {
        AuthenticationScreen(
            navHostController = navHostController,
            backPressedDispatcher = onBackPressedDispatcher
        )
    }
}

@Serializable
object AuthenticationScreen