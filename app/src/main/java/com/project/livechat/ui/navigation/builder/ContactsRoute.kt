package com.project.livechat.ui.navigation.builder

import androidx.activity.OnBackPressedDispatcher
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.project.livechat.ui.screens.contacts.ContactsScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.contactsRoute(
    navHostController: NavHostController,
    onBackPressedDispatcher: OnBackPressedDispatcher,
) {
    composable<ContactsScreen> {
        ContactsScreen(
            navHostController = navHostController,
            backPressedDispatcher = onBackPressedDispatcher
        )
    }
}

@Serializable
object ContactsScreen