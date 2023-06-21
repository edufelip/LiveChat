package com.project.livechat.ui.navigation.builder

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.project.livechat.ui.navigation.Routes
import com.project.livechat.ui.screens.contacts.ContactsScreen
import com.project.livechat.ui.utils.exitTransition
import com.project.livechat.ui.utils.popEnterTransition
import com.project.livechat.ui.viewmodels.ContactsViewModel
import com.project.livechat.ui.viewmodels.PermissionViewModel

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.contactsRoute(
    navHostController: NavHostController,
    onBackPressedDispatcher: OnBackPressedDispatcher,
    permissionViewModel: PermissionViewModel,
    contactsViewModel: ContactsViewModel
) {
    composable(
        route = Routes.ContactsRoute.route,
        popEnterTransition = { popEnterTransition },
        exitTransition = { exitTransition },
    ) {
        ContactsScreen(
            navHostController = navHostController,
            backPressedDispatcher = onBackPressedDispatcher,
            permissionViewModel = permissionViewModel,
            contactsViewModel = contactsViewModel
        )
    }
}