package com.project.livechat.ui.screens.onboarding.pagerViews.success

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.livechat.ui.screens.onboarding.models.NumberVerificationFormState
import com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification.StepIndicators
import com.project.livechat.ui.theme.LiveChatTheme

@Composable
fun OnBoardingSuccess(
    state: NumberVerificationFormState,
    onStartChatting: () -> Unit
) {
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
    val goldColor = Color(0xFFFFD700)
    val goldLight = Color(0xFFFFFACD)
    val displayFont = FontFamily.Serif
    val sansFont = FontFamily.SansSerif

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SuccessIllustration(
                    primaryColor = primaryColor,
                    goldColor = goldColor,
                    goldLight = goldLight
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "You're all set!",
                    color = textColor,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = displayFont,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 40.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your account has been created successfully.",
                    color = textColor.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = sansFont,
                        fontWeight = FontWeight.Light,
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
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
                    onClick = onStartChatting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = textColor
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start Chatting",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = sansFont,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessIllustration(
    primaryColor: Color,
    goldColor: Color,
    goldLight: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(180.dp)
            .background(
                color = primaryColor.copy(alpha = 0.25f),
                shape = CircleShape
            )
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            tonalElevation = 8.dp,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .border(
                    width = 6.dp,
                    color = primaryColor,
                    shape = CircleShape
                )
        ) {}

        Icon(
            imageVector = Icons.Outlined.SentimentVerySatisfied,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(88.dp)
        )

        Icon(
            imageVector = Icons.Outlined.WorkspacePremium,
            contentDescription = null,
            tint = goldColor,
            modifier = Modifier.size(52.dp)
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(goldLight.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnBoardingSuccessPreview() {
    LiveChatTheme {
        OnBoardingSuccess(
            state = NumberVerificationFormState(currentPage = 2, totalPages = 3),
            onStartChatting = {}
        )
    }
}
