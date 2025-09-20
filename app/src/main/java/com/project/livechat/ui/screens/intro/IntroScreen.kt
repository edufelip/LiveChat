package com.project.livechat.ui.screens.intro

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.utils.extensions.AnnotatedStrStruct
import com.project.livechat.ui.utils.extensions.AnnotatedStructType
import com.project.livechat.ui.utils.extensions.LinkText
import com.project.livechat.ui.utils.extensions.buildLinkText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun IntroScreen(
    onGetStarted: () -> Unit,
    onLogin: (() -> Unit)? = null,
    onSignUp: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val annotatedString = buildLinkText(
        listOf(
            AnnotatedStrStruct(
                text = "By joining, you agree to the ",
                type = AnnotatedStructType.REGULAR
            ),
            AnnotatedStrStruct(
                text = "privacy policy",
                type = AnnotatedStructType.LINK(tag = "policy")
            ),
            AnnotatedStrStruct(text = " and ", type = AnnotatedStructType.REGULAR),
            AnnotatedStrStruct(
                text = "terms of use",
                type = AnnotatedStructType.LINK(tag = "terms")
            )
        ),
        MaterialTheme.colorScheme.primary
    )

    IntroContent(
        context = context,
        annotatedString = annotatedString,
        onGetStarted = onGetStarted,
        onLogin = onLogin,
        onSignUp = onSignUp
    )
}

@Composable
private fun IntroContent(
    context: Context,
    annotatedString: AnnotatedString,
    onGetStarted: () -> Unit,
    onLogin: (() -> Unit)?,
    onSignUp: (() -> Unit)?
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
    val secondaryColor = if (isDarkMode) Color(0xFF6A8D8A) else Color(0xFFCFE8E6)
    val textOnBackground = if (isDarkMode) Color(0xFFD1E0DD) else Color(0xFF3F5A57)

    val displayFont = FontFamily.Serif
    val sansFont = FontFamily.SansSerif

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
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
                IntroIllustration(
                    modifier = Modifier.size(256.dp),
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    accentColor = primaryColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Live Chat",
                    color = textOnBackground,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = displayFont,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 48.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Connect with elegance.",
                    color = textOnBackground.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = sansFont,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    color = primaryColor,
                    shape = RoundedCornerShape(999.dp),
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .height(52.dp),
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = textOnBackground
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 24.dp,
                                vertical = 10.dp
                            ),
                            onClick = onGetStarted
                        ) {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = sansFont,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        if (onLogin != null || onSignUp != null) {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = secondaryColor,
                                    contentColor = textOnBackground
                                ),
                                onClick = { onLogin?.invoke() ?: onGetStarted() }
                            ) {
                                Text(
                                    text = "Log In",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = sansFont,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = textOnBackground,
                                    contentColor = Color.White
                                ),
                                onClick = { onSignUp?.invoke() ?: onGetStarted() }
                            ) {
                                Text(
                                    text = "Sign Up",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = sansFont,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }

                LinkText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = sansFont,
                        color = textOnBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) { tag ->
                    val message = when (tag) {
                        "policy" -> "To be implemented yet - Privacy Policy"
                        "terms" -> "To be implemented yet - Terms of Service"
                        else -> null
                    }
                    message?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroIllustration(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    val transition = rememberInfiniteTransition(label = "intro_illustration")
    val fullRotation = (2 * PI).toFloat()

    val orbitA by transition.animateFloat(
        initialValue = 0f,
        targetValue = fullRotation,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_a"
    )
    val orbitB by transition.animateFloat(
        initialValue = 0f,
        targetValue = fullRotation,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_b"
    )
    val orbitC by transition.animateFloat(
        initialValue = 0f,
        targetValue = fullRotation,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 19000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_c"
    )
    val orbitD by transition.animateFloat(
        initialValue = 0f,
        targetValue = fullRotation,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 21000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_d"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(x = width / 2f, y = height / 2f)
        val minDimension = min(width, height)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.45f), Color.Transparent),
                center = center,
                radius = minDimension * 0.55f
            )
        )

        drawCircle(
            color = secondaryColor.copy(alpha = 0.9f),
            radius = minDimension * 0.36f,
            center = Offset(
                x = center.x + width * 0.12f + sin(orbitA.toDouble()).toFloat() * width * 0.035f,
                y = center.y - height * 0.08f + cos(orbitA.toDouble()).toFloat() * height * 0.03f
            )
        )

        drawCircle(
            color = primaryColor.copy(alpha = 0.8f),
            radius = minDimension * 0.28f,
            center = Offset(
                x = center.x - width * 0.18f + cos(orbitB.toDouble()).toFloat() * width * 0.04f,
                y = center.y + height * 0.05f + sin(orbitB.toDouble()).toFloat() * height * 0.045f
            )
        )

        drawCircle(
            color = accentColor.copy(alpha = 0.6f),
            radius = minDimension * 0.2f,
            center = Offset(
                x = center.x + width * 0.18f + sin(orbitC.toDouble()).toFloat() * width * 0.05f,
                y = center.y + height * 0.22f + cos(orbitC.toDouble()).toFloat() * height * 0.04f
            )
        )

        drawCircle(
            color = secondaryColor.copy(alpha = 0.35f),
            radius = minDimension * 0.14f,
            center = Offset(
                x = center.x + cos((orbitD + fullRotation / 3f).toDouble()).toFloat() * width * 0.12f,
                y = center.y + sin((orbitD + fullRotation / 3f).toDouble()).toFloat() * height * 0.08f
            )
        )

        val glowPaint = Paint().apply {
            color = Color.White.copy(alpha = 0.08f)
        }
        drawIntoCanvas { canvas ->
            canvas.drawCircle(center, minDimension * 0.6f, glowPaint)
        }
    }
}

@Preview
@Composable
private fun IntroScreenPreview() {
    LiveChatTheme {
        IntroScreen(
            onGetStarted = {},
            onLogin = {},
            onSignUp = {}
        )
    }
}
