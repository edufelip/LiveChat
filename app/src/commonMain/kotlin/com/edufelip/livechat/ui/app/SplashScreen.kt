package com.edufelip.livechat.ui.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.resources.Res
import com.edufelip.livechat.resources.livechat_logo
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SplashScreen(
    message: String,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    val brandName = strings.onboarding.welcomeBrandName
    val colors = MaterialTheme.colorScheme
    val gradient =
        remember(colors) {
            Brush.verticalGradient(
                listOf(
                    colors.surface,
                    colors.surface.copy(alpha = 0.96f),
                    colors.primary.copy(alpha = 0.06f),
                    colors.tertiary.copy(alpha = 0.08f),
                ),
            )
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minSize = size.minDimension
            drawCircle(
                color = colors.primary.copy(alpha = 0.08f),
                radius = minSize * 0.42f,
                center = Offset(size.width * 0.18f, size.height * 0.22f),
            )
            drawCircle(
                color = colors.secondary.copy(alpha = 0.07f),
                radius = minSize * 0.32f,
                center = Offset(size.width * 0.86f, size.height * 0.28f),
            )
            drawCircle(
                color = colors.tertiary.copy(alpha = 0.08f),
                radius = minSize * 0.46f,
                center = Offset(size.width * 0.5f, size.height * 0.9f),
            )
        }

        Box(
            modifier =
                Modifier
                    .size(220.dp)
                    .blur(48.dp)
                    .background(colors.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = colors.surfaceContainer,
                tonalElevation = 2.dp,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.livechat_logo),
                        contentDescription = brandName,
                        modifier = Modifier.size(56.dp),
                    )
                }
            }
            Text(
                text = brandName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.onBackground,
            )
            Spacer(modifier = Modifier.size(MaterialTheme.spacing.xs))
            CircularProgressIndicator(
                color = colors.primary,
                strokeWidth = 3.dp,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
