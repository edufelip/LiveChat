package com.project.livechat.composeapp.ui.app.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.project.livechat.composeapp.ui.app.AppIcons
import com.project.livechat.domain.models.HomeTab

data class HomeTabItem(
    val tab: HomeTab,
    val label: String,
    val icon: ImageVector,
)

val defaultHomeTabs =
    listOf(
        HomeTabItem(
            tab = HomeTab.Conversations,
            label = "Chats",
            icon = AppIcons.conversations,
        ),
        HomeTabItem(
            tab = HomeTab.Contacts,
            label = "Contacts",
            icon = AppIcons.contacts,
        ),
    )
