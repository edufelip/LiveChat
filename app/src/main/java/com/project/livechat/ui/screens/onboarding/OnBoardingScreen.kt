package com.project.livechat.ui.screens.onboarding

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.project.livechat.ui.screens.onboarding.pagerViews.OnBoardingTermsAgreement
import com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification.OnBoardingNumberVerification
import com.project.livechat.ui.viewmodels.OnBoardingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(
    navHostController: NavHostController,
    backPressedDispatcher: OnBackPressedDispatcher,
    onBoardingViewModel: OnBoardingViewModel
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    val totalPages = onBoardingViewModel.pages
    val currentPage = onBoardingViewModel.state.currentPage

    LaunchedEffect(key1 = currentPage) {
        scope.launch {
            pagerState.animateScrollToPage(currentPage)
        }
    }

    OnBoardingContent(
        pagerState = pagerState,
        navHostController = navHostController,
        onBoardingViewModel = onBoardingViewModel,
        totalPages = totalPages
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingContent(
    onBoardingViewModel: OnBoardingViewModel,
    navHostController: NavHostController,
    pagerState: PagerState,
    totalPages: Int
) {
    HorizontalPager(pageCount = totalPages, state = pagerState) { index ->
        when (index) {
            0 -> OnBoardingTermsAgreement(onBoardingViewModel = onBoardingViewModel)
            1 -> OnBoardingNumberVerification(onBoardingViewModel = onBoardingViewModel)
        }
    }
}