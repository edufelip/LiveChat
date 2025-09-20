package com.project.livechat.ui.screens.onboarding.pagerViews.oneTimePassword

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.livechat.ui.screens.onboarding.models.NumberVerificationFormState
import com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification.NumberVerificationFormEvent
import com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification.StepIndicators
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.utils.extensions.AnnotatedStrStruct
import com.project.livechat.ui.utils.extensions.AnnotatedStructType
import com.project.livechat.ui.utils.extensions.LinkText
import com.project.livechat.ui.utils.extensions.buildLinkText
import com.project.livechat.ui.utils.extensions.toastShort
import com.project.livechat.ui.viewmodels.OnBoardingViewModel
import kotlin.math.max
import kotlin.math.min

@Composable
fun OnBoardingOneTimePassword(onBoardingViewModel: OnBoardingViewModel) {
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

@Composable
fun OneTimePasswordContent(
    state: NumberVerificationFormState,
    annotatedString: AnnotatedString,
    events: OneTimePasswordEvents
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDarkMode = isSystemInDarkTheme()
    val backgroundBrush = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1A2A2A), Color(0xFF2A403F)),
            start = Offset.Zero,
            end = Offset(1000f, 1400f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFF0FDFA), Color(0xFFCFE8E6)),
            start = Offset.Zero,
            end = Offset(1000f, 1400f)
        )
    }
    val primaryColor = if (isDarkMode) Color(0xFF80CBC4) else Color(0xFFB2DFDB)
    val textColor = if (isDarkMode) Color(0xFFD1E0DD) else Color(0xFF3F5A57)
    val inputBackground = if (isDarkMode) Color(0xFF2F4341) else Color(0xFFE0F2F1)
    val displayFont = FontFamily.Serif
    val sansFont = FontFamily.SansSerif
    val remainingSeconds = events.timeoutValue()
    val formattedTimer = formatSeconds(remainingSeconds)
    val verifyEnabled = state.oneTimePass.length == 6

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                IconButton(onClick = {
                    if (events.timesUpValue()) {
                        events.navigateBackwards()
                    } else {
                        context.toastShort("Wait until verification count finishes")
                    }
                }) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Enter OTP",
                    color = textColor,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = displayFont,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "A 6-digit code has been sent to your phone number.",
                    color = textColor.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = sansFont
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                BasicTextField(
                    value = state.oneTimePass,
                    onValueChange = {
                        val digitsOnly = it.filter(Char::isDigit)
                        if (digitsOnly.length <= 6) {
                            events.validationEventOneTimePassChanged(digitsOnly)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    decorationBox = { innerTextField ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(
                                space = 12.dp,
                                alignment = Alignment.CenterHorizontally
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(6) { index ->
                                val char = state.oneTimePass.getOrNull(index)?.toString().orEmpty()
                                val isFocused = state.oneTimePass.length == index
                                val borderColor = when {
                                    char.isNotEmpty() -> primaryColor
                                    isFocused -> primaryColor.copy(alpha = 0.6f)
                                    else -> textColor.copy(alpha = 0.25f)
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .width(44.dp)
                                        .height(56.dp)
                                        .background(
                                            color = inputBackground,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = borderColor,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Text(
                                        text = char,
                                        color = textColor,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = displayFont,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (events.timesUpValue()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Didn't receive the code?",
                            color = textColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinkText(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = primaryColor,
                                fontFamily = sansFont,
                                fontWeight = FontWeight.Medium
                            )
                        ) { tag ->
                            if (tag == "try_again") {
                                events.callSmsVerification()
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resend code in ",
                            color = textColor,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = sansFont
                            )
                        )
                        Text(
                            text = formattedTimer,
                            color = primaryColor,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = sansFont,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StepIndicators(
                    totalSteps = state.totalPages,
                    currentStep = state.currentPage,
                    activeColor = primaryColor,
                    inactiveColor = textColor.copy(alpha = 0.3f)
                )
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        events.submitAction()
                    },
                    enabled = verifyEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = textColor,
                        disabledContainerColor = primaryColor.copy(alpha = 0.4f),
                        disabledContentColor = textColor.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Verify",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = sansFont,
                            fontWeight = FontWeight.Medium
                        )
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
                phoneNum = "21985670564",
                countryIso = "BR",
                currentPage = 1,
                totalPages = 3
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

private fun formatSeconds(totalSeconds: Int): String {
    val safeSeconds = max(totalSeconds, 0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
