package com.project.livechat.ui.screens.onboarding.pagerViews.oneTimePassword

import android.app.Activity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.livechat.ui.screens.onboarding.models.NumberVerificationFormState
import com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification.NumberVerificationFormEvent
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.utils.extensions.AnnotatedStrStruct
import com.project.livechat.ui.utils.extensions.AnnotatedStructType
import com.project.livechat.ui.utils.extensions.buildLinkText
import com.project.livechat.ui.utils.extensions.toastShort
import com.project.livechat.ui.viewmodels.OnBoardingViewModel
import com.project.livechat.ui.widgets.topbar.DefaultTopBar

@Composable
fun OnBoardingOneTimePassword(onBoardingViewModel: OnBoardingViewModel = hiltViewModel()) {
    val activity = LocalContext.current as Activity
    val state = onBoardingViewModel.screenState
    val timerCount = onBoardingViewModel.timeoutCount.collectAsStateWithLifecycle()
    val timesUp = remember {
        derivedStateOf { timerCount.value == 0 }
    }

    val annotatedString = buildLinkText(
        listOf(
            AnnotatedStrStruct(
                text = "Try again",
                type = AnnotatedStructType.LINK(tag = "try_again")
            )
        ),
        MaterialTheme.colorScheme.primary
    )

    OneTimePasswordContent(
        state = state,
        annotatedString = annotatedString,
        events = OneTimePasswordEvents(
            navigateBackwards = {
                onBoardingViewModel.navigateBackwards()
            },
            callSmsVerification = {
                onBoardingViewModel.callSmsVerification(
                    activity,
                    onBoardingViewModel.callbacks
                )
            },
            validationEventOneTimePassChanged = {
                onBoardingViewModel.onValidationEvent(
                    NumberVerificationFormEvent.OneTimePassChanged(
                        it
                    )
                )
            },
            timeoutValue = {
                timerCount.value
            },
            timesUpValue = {
                timesUp.value
            },
            submitAction = {
                onBoardingViewModel.submitCodeVerification()
            }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneTimePasswordContent(
    state: NumberVerificationFormState,
    annotatedString: AnnotatedString,
    events: OneTimePasswordEvents
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            DefaultTopBar(
                title = "Voltar",
                onBackClick = {
                    if (events.timesUpValue()) {
                        events.navigateBackwards()
                    } else {
                        context.toastShort("Wait until verification count finishes")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Please verify your account",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "We just sent an SMS to ${state.fullNumber} with your verification code",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            BasicTextField(
                value = state.oneTimePass,
                onValueChange = {
                    if (it.length <= 6)
                        events.validationEventOneTimePassChanged(it)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword
                ),
                decorationBox = {
                    Row(horizontalArrangement = Arrangement.Center) {
                        repeat(6) { index ->
                            val char = when {
                                index >= state.oneTimePass.length -> ""
                                else -> state.oneTimePass[index].toString()
                            }
                            Text(
                                modifier = Modifier
                                    .width(40.dp)
                                    .border(
                                        1.dp,
                                        Color.LightGray,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(2.dp),
                                text = char,
                                style = MaterialTheme.typography.headlineLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            FilledTonalButton(
                modifier = Modifier.width(128.dp),
                enabled = !events.timesUpValue(),
                onClick = {
                    events.submitAction()
                }
            ) {
                Text(text = "Submit")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (events.timesUpValue()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Didn't receive the code?")
                    Spacer(modifier = Modifier.height(4.dp))
                    ClickableText(text = annotatedString, onClick = { offset ->
                        annotatedString.getStringAnnotations(
                            tag = "try_again",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            events.callSmsVerification()
                        }
                    })
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null
                    )
                    Text(
                        text = "0:${
                            with(events.timeoutValue()) {
                                if (this < 10) "0${this}" else this
                            }
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun OnBoardingOneTimePasswordPreview() {
    LiveChatTheme {
        OneTimePasswordContent(
            state = NumberVerificationFormState(
                phoneCode = "55",
                phoneNum = "21985670564"
            ),
            annotatedString = buildLinkText(
                listOf(
                    AnnotatedStrStruct(
                        text = "Try again",
                        type = AnnotatedStructType.LINK(tag = "try_again")
                    )
                ),
                MaterialTheme.colorScheme.primary
            ),
            events = OneTimePasswordEvents.mock
        )
    }
}

data class OneTimePasswordEvents(
    val navigateBackwards: () -> Unit,
    val callSmsVerification: () -> Unit,
    val validationEventOneTimePassChanged: (text: String) -> Unit,
    val timeoutValue: () -> Int,
    val timesUpValue: () -> Boolean,
    val submitAction: () -> Unit
) {
    companion object {
        val mock = OneTimePasswordEvents(
            navigateBackwards = {},
            callSmsVerification = {},
            validationEventOneTimePassChanged = {},
            timeoutValue = { 45 },
            timesUpValue = { true },
            submitAction = {}
        )
    }
}