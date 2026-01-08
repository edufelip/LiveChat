package com.edufelip.livechat.ui.app.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.ui.app.AppIcons
import com.edufelip.livechat.ui.resources.HomeStrings

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
            tab = HomeTab.Calls,
            labelSelector = { it.callsTab },
            icon = AppIcons.calls,
        ),
        HomeTabItem(
            tab = HomeTab.Settings,
            labelSelector = { it.settingsTab },
            icon = AppIcons.settings,
        ),
    )
