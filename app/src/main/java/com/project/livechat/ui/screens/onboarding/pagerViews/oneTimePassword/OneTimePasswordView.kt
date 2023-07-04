package com.project.livechat.ui.screens.onboarding.pagerViews.oneTimePassword

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification.NumberVerificationFormEvent
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.utils.extensions.AnnotatedStrStruct
import com.project.livechat.ui.utils.extensions.AnnotatedStructType
import com.project.livechat.ui.utils.extensions.buildLinkText
import com.project.livechat.ui.viewmodels.OnBoardingViewModel
import com.project.livechat.ui.widgets.topbar.DefaultTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingOneTimePassword(onBoardingViewModel: OnBoardingViewModel) {
    val context = LocalContext.current
    val activity = context as Activity
    val state = onBoardingViewModel.screenState

    val annotatedString = buildLinkText(
        listOf(
            AnnotatedStrStruct(
                text = "Try again",
                type = AnnotatedStructType.LINK(tag = "try_again")
            )
        ),
        MaterialTheme.colorScheme.primary
    )


    LaunchedEffect(key1 = Unit) {
    }

    Scaffold(
        topBar = {
            DefaultTopBar(
                title = "Voltar",
                onBackClick = { onBoardingViewModel.navigateBackwards() }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Please verify your account")
            Text(
                textAlign = TextAlign.Center,
                text = "We just sent an SMS to ${state.fullNumber} with your verification code"
            )
            OutlinedTextField(
                value = state.oneTimePass,
                onValueChange = {
                    onBoardingViewModel.onValidationEvent(NumberVerificationFormEvent.OneTimePassChanged(it))
                }
            )
            Button(onClick = {
                // Verify the code
            }) {
                Text(text = "Submit")
            }

            Text(text = "Didn't receive the code?")
            ClickableText(text = annotatedString, onClick = { offset ->
                annotatedString.getStringAnnotations(
                    tag = "try_again",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let {
                    // Try again
                }
            })
        }
    }
}

@Preview
@Composable
fun OnBoardingOneTimePasswordPreview() {
    val onBoardingViewModel = hiltViewModel<OnBoardingViewModel>()
    LiveChatTheme {
        OnBoardingOneTimePassword(onBoardingViewModel)
    }
}

