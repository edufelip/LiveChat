package com.project.livechat.ui.utils.extensions

import androidx.compose.foundation.pager.PagerState

fun PagerState.isIndexCurrent(index: Int): Boolean {
    return this.currentPage == index
}