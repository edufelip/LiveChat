package com.edufelip.livechat.ui.features.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.onboarding.OnboardingTestTags
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SuccessStep(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings().onboarding
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(OnboardingTestTags.SUCCESS_STEP)
                .padding(horizontal = MaterialTheme.spacing.xxl, vertical = MaterialTheme.spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl, Alignment.CenterVertically),
    ) {
        Text(
            text = strings.successTitle,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        Text(
            text = strings.successSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onFinished,
            modifier =
                Modifier
                    .heightIn(min = 48.dp)
                    .testTag(OnboardingTestTags.SUCCESS_BUTTON),
        ) {
            Text(strings.successCta)
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun SuccessStepPreview() {
    LiveChatPreviewContainer {
        SuccessStep(onFinished = {})
    }
}
