package com.project.livechat.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.project.livechat.ui.navigation.Routes
import com.project.livechat.ui.navigation.builder.contactsRoute
import com.project.livechat.ui.navigation.builder.homeRoute
import com.project.livechat.ui.navigation.builder.onBoardingRoute
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.viewmodels.ContactsViewModel
import com.project.livechat.ui.viewmodels.PermissionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveChatTheme {
                val navController = rememberAnimatedNavController()
                val permissionViewModel = viewModel<PermissionViewModel>()
                val contactsViewModel = viewModel<ContactsViewModel>()
                contentResolver
                AnimatedNavHost(
                    navController = navController,
                    startDestination = Routes.OnBoardingRoute.route,
                    builder = {
                        onBoardingRoute(
                            navHostController = navController,
                            onBackPressedDispatcher = onBackPressedDispatcher
                        )
                        homeRoute(
                            navHostController = navController,
                            onBackPressedDispatcher = onBackPressedDispatcher
                        )
                        contactsRoute(
                            navHostController = navController,
                            onBackPressedDispatcher = onBackPressedDispatcher,
                            permissionViewModel = permissionViewModel,
                            contactsViewModel = contactsViewModel
                        )
                    }
                )
            }
        }
    }
}