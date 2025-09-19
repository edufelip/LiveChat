package com.project.livechat.ui.navigation.builder

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.project.livechat.ui.screens.contacts.ContactsScreen
import com.project.livechat.ui.viewmodels.ContactsViewModel
import com.project.livechat.ui.viewmodels.PermissionViewModel
import org.koin.androidx.compose.koinViewModel
import kotlinx.serialization.Serializable

fun NavGraphBuilder.contactsRoute(
    navHostController: NavHostController,
) {
    composable<ContactsScreen> { backStackEntry ->
        val permissionViewModel: PermissionViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)
        val contactsViewModel: ContactsViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)
        ContactsScreen(
            navHostController = navHostController,
            permissionViewModel = permissionViewModel,
            contactsViewModel = contactsViewModel
        )
    }
}

@Serializable
object ContactsScreen
