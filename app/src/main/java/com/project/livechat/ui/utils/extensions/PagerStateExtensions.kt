package com.project.livechat.ui.utils.extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState

@OptIn(ExperimentalFoundationApi::class)
fun PagerState.isIndexCurrent(index: Int): Boolean {
    return this.currentPage == index
}