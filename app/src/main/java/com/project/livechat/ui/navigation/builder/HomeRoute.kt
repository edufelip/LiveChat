package com.project.livechat.ui.navigation.builder

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.project.livechat.ui.screens.home.HomeScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.homeRoute(
    navHostController: NavHostController
) {
    composable<HomeScreen>{
        HomeScreen(
            navHostController = navHostController,
            onConversationSelected = { /* TODO: navigate to conversation detail when available */ },
            onContactsClick = { navHostController.navigate(ContactsScreen) }
        )
    }
}

@Serializable
object HomeScreen
