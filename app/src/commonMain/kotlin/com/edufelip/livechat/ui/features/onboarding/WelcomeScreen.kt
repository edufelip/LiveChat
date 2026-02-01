package com.edufelip.livechat.ui.features.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.resources.Res
import com.edufelip.livechat.resources.livechat_logo
import com.edufelip.livechat.ui.platform.openWebViewUrl
import com.edufelip.livechat.ui.resources.liveChatStrings
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun WelcomeScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings().onboarding
    val colors = MaterialTheme.colorScheme
    val termsStyle =
        MaterialTheme.typography.bodySmall.copy(
            color = colors.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
    val termsAnnotated =
        remember(strings.welcomeTermsMessage, colors.primary) {
            val termsKeyword = "Terms of Service"
            val privacyKeyword = "Privacy Policy"
            buildAnnotatedString {
                append(strings.welcomeTermsMessage)
                val termsIndex = strings.welcomeTermsMessage.indexOf(termsKeyword)
                if (termsIndex >= 0) {
                    addStyle(
                        style = SpanStyle(color = colors.primary, fontWeight = FontWeight.SemiBold),
                        start = termsIndex,
                        end = termsIndex + termsKeyword.length,
                    )
                    addStringAnnotation(
                        tag = "terms",
                        annotation = termsKeyword,
                        start = termsIndex,
                        end = termsIndex + termsKeyword.length,
                    )
                }
                val privacyIndex = strings.welcomeTermsMessage.indexOf(privacyKeyword)
                if (privacyIndex >= 0) {
                    addStyle(
                        style = SpanStyle(color = colors.primary, fontWeight = FontWeight.SemiBold),
                        start = privacyIndex,
                        end = privacyIndex + privacyKeyword.length,
                    )
                    addStringAnnotation(
                        tag = "privacy",
                        annotation = privacyKeyword,
                        start = privacyIndex,
                        end = privacyIndex + privacyKeyword.length,
                    )
                }
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(OnboardingTestTags.WELCOME_STEP),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WelcomeHeroIllustration(
                modifier = Modifier.fillMaxWidth(),
                brandName = strings.welcomeBrandName,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = strings.welcomeTitle,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = strings.welcomeSubtitle,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag(OnboardingTestTags.WELCOME_CTA),
                onClick = onContinue,
                shape = RoundedCornerShape(18.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary,
                    ),
            ) {
                Text(
                    text = strings.welcomeCta,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ClickableText(
                text = termsAnnotated,
                modifier = Modifier.fillMaxWidth(),
                onClick = { offset ->
                    termsAnnotated.getStringAnnotations(tag = "terms", start = offset, end = offset)
                        .firstOrNull()
                        ?.let {
                            if (strings.welcomeTermsUrl.isNotBlank()) {
                                openWebViewUrl(strings.welcomeTermsUrl)
                            }
                        }
                    termsAnnotated.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                        .firstOrNull()
                        ?.let {
                            if (strings.welcomePrivacyUrl.isNotBlank()) {
                                openWebViewUrl(strings.welcomePrivacyUrl)
                            }
                        }
                },
                style = termsStyle,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun WelcomeHeroIllustration(
    brandName: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val brandScrim =
        remember(colors.surface) {
            Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    colors.surface.copy(alpha = 0.7f),
                    Color.Transparent,
                ),
            )
        }
    val primary = colors.primary
    val secondary = colors.secondary
    val tertiary = colors.tertiary
    val surface = colors.surface
    val onSurface = colors.onSurface

    Box(
        modifier =
            modifier
                .aspectRatio(4f / 3f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(220.dp)
                    .blur(42.dp)
                    .background(primary.copy(alpha = 0.12f), CircleShape),
        )
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val phoneWidth = size.width * 0.38f
            val phoneHeight = size.height * 0.62f
            val phoneTopLeft =
                Offset(
                    x = (size.width - phoneWidth) / 2f,
                    y = size.height * 0.18f,
                )
            val phoneCorner = 26.dp.toPx()
            drawRoundRect(
                color = surface,
                topLeft = phoneTopLeft,
                size = Size(phoneWidth, phoneHeight),
                cornerRadius = CornerRadius(phoneCorner, phoneCorner),
            )
            val screenInset = 10.dp.toPx()
            val screenCorner = (phoneCorner - 8.dp.toPx()).coerceAtLeast(0f)
            drawRoundRect(
                color = primary.copy(alpha = 0.08f),
                topLeft = phoneTopLeft + Offset(screenInset, screenInset),
                size =
                    Size(
                        phoneWidth - screenInset * 2,
                        phoneHeight - screenInset * 2,
                    ),
                cornerRadius = CornerRadius(screenCorner, screenCorner),
            )
            drawRoundRect(
                color = onSurface.copy(alpha = 0.12f),
                topLeft =
                    Offset(
                        x = phoneTopLeft.x + phoneWidth * 0.28f,
                        y = phoneTopLeft.y + 12.dp.toPx(),
                    ),
                size =
                    Size(
                        phoneWidth * 0.44f,
                        6.dp.toPx(),
                    ),
                cornerRadius =
                    CornerRadius(
                        6.dp.toPx(),
                        6.dp.toPx(),
                    ),
            )

            val bubbleCorner = 18.dp.toPx()
            drawRoundRect(
                color = primary.copy(alpha = 0.18f),
                topLeft =
                    Offset(
                        x = size.width * 0.12f,
                        y = size.height * 0.24f,
                    ),
                size =
                    Size(
                        size.width * 0.34f,
                        size.height * 0.18f,
                    ),
                cornerRadius = CornerRadius(bubbleCorner, bubbleCorner),
            )
            drawRoundRect(
                color = secondary.copy(alpha = 0.2f),
                topLeft =
                    Offset(
                        x = size.width * 0.56f,
                        y = size.height * 0.56f,
                    ),
                size =
                    Size(
                        size.width * 0.3f,
                        size.height * 0.16f,
                    ),
                cornerRadius = CornerRadius(bubbleCorner, bubbleCorner),
            )

            drawCircle(
                color = tertiary.copy(alpha = 0.25f),
                radius = 16.dp.toPx(),
                center = Offset(size.width * 0.28f, size.height * 0.68f),
            )
            drawCircle(
                color = secondary.copy(alpha = 0.25f),
                radius = 14.dp.toPx(),
                center = Offset(size.width * 0.72f, size.height * 0.34f),
            )

            drawCircle(
                color = onSurface.copy(alpha = 0.08f),
                radius = 4.dp.toPx(),
                center = Offset(size.width * 0.12f, size.height * 0.72f),
            )
            drawCircle(
                color = onSurface.copy(alpha = 0.08f),
                radius = 3.dp.toPx(),
                center = Offset(size.width * 0.88f, size.height * 0.28f),
            )

            drawCircle(
                color = primary.copy(alpha = 0.18f),
                radius = 60.dp.toPx(),
                center = Offset(size.width * 0.18f, size.height * 0.58f),
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-48).dp)
                    .padding(top = 8.dp)
                    .background(brandScrim, RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.livechat_logo),
                contentDescription = brandName,
                modifier =
                    Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = brandName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface,
            )
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun WelcomeScreenPreview() {
    LiveChatPreviewContainer {
        Surface {
            WelcomeScreen(onContinue = {})
        }
    }
}
