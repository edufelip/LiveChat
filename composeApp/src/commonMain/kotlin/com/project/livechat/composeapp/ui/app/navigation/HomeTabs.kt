package com.project.livechat.composeapp.ui.app.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.project.livechat.composeapp.ui.app.AppIcons
import com.project.livechat.domain.models.HomeTab

import com.project.livechat.composeapp.ui.resources.HomeStrings

data class HomeTabItem(
    val tab: HomeTab,
    val labelSelector: (HomeStrings) -> String,
    val icon: ImageVector,
)

val defaultHomeTabs =
    listOf(
        HomeTabItem(
            tab = HomeTab.Conversations,
            labelSelector = { it.chatsTab },
            icon = AppIcons.conversations,
        ),
        HomeTabItem(
            tab = HomeTab.Contacts,
            labelSelector = { it.contactsTab },
            icon = AppIcons.contacts,
        ),
        HomeTabItem(
            tab = HomeTab.Settings,
            labelSelector = { it.settingsTab },
            icon = AppIcons.settings,
        ),
    )
