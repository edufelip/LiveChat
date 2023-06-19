package com.project.livechat.ui.navigation

import androidx.navigation.NavHostController

sealed class Routes(val route: String, val argumentKey: String) {
    fun navigate(navHostController: NavHostController) {
        navHostController.navigate(route)
    }

    fun <T> navigateWithArgument(
        navHostController: NavHostController,
        argumentValue: T?
    ) {
        navHostController.currentBackStackEntry?.savedStateHandle?.set(
            key = argumentKey,
            value = argumentValue
        )
        navigate(navHostController)
    }

    fun <T> navigateWithArgumentList(
        navHostController: NavHostController,
        argumentValue: List<T>?
    ) {
        navHostController.currentBackStackEntry?.savedStateHandle?.set(
            key = argumentKey,
            value = argumentValue
        )
        navigate(navHostController)
    }

    object OnBoardingRoute : Routes(
        route = "on_boarding_screen",
        argumentKey = "ARGS-HOME-SCREEN"
    )

    object HomeRoute : Routes(
        route = "home_screen",
        argumentKey = "ARGS-HOME-SCREEN"
    )

    object AuthenticationRoute : Routes(
        route = "authentication_route",
        argumentKey = "ARGS-AUTH-SCREEN"
    )

    object NewChatRoute : Routes(
        route = "new_chat_screen",
        argumentKey = "ARGS-NEW-CHAT-SCREEN"
    )

    object ChatRoute : Routes(
        route = "chat_screen",
        argumentKey = "ARGS-CHAT-SCREEN"
    )

    object ContactsRoute: Routes(
        route = "home_screen/contacts_screen",
        argumentKey = "ARGS-CONTACTS-SCREEN"
    )
}