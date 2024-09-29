package com.project.livechat.ui.screens.home.behavior

import androidx.compose.foundation.pager.PagerState
import kotlinx.coroutines.CoroutineScope

abstract class HomeTabBehavior {
    abstract fun execute()
}

enum class HomeTabCategory {
    FILTER, ADD
}

fun homeTabBehaviorFactory(
    behavior: HomeTabCategory,
    pagerState: PagerState,
    scope: CoroutineScope,
    index: Int
): HomeTabBehavior {
    return when(behavior) {
        HomeTabCategory.FILTER -> HomeTabFilterBehavior(pagerState, scope, index)
        HomeTabCategory.ADD -> HomeTabAddBehavior()
    }
}