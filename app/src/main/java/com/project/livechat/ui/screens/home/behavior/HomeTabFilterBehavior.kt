package com.project.livechat.ui.screens.home.behavior

import androidx.compose.foundation.pager.PagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HomeTabFilterBehavior(
    private val pagerState: PagerState,
    private val scope: CoroutineScope,
    private val index: Int
) : HomeTabBehavior() {
    override fun execute() {
        scope.launch {
            pagerState.animateScrollToPage(index)
        }
    }
}