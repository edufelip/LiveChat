package com.project.livechat.ui.screens.onboarding.pagerViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.livechat.ui.viewmodels.OnBoardingViewModel
import com.project.livechat.ui.theme.LiveChatTheme

@Composable
fun OnBoardingOneTimePassword(onBoardingViewModel: OnBoardingViewModel) {

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {

    }
}

@Preview
@Composable
fun OnBoardingOneTimePasswordPreview() {
    val onBoardingViewModel = viewModel<OnBoardingViewModel>()
    LiveChatTheme {
        OnBoardingOneTimePassword(onBoardingViewModel)
    }
}