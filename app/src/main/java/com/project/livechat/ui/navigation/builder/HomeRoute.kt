package com.project.livechat.ui.navigation.builder

import androidx.activity.OnBackPressedDispatcher
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.project.livechat.ui.screens.home.HomeScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.homeRoute(
    navHostController: NavHostController,
    onBackPressedDispatcher: OnBackPressedDispatcher
) {
    composable<HomeScreen>{
        HomeScreen(navHostController = navHostController, backPressedDispatcher = onBackPressedDispatcher)
    }
}

@Serializable
object HomeScreen