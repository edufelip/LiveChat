package com.project.livechat.ui.screens.onboarding

import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project.livechat.ui.theme.LiveChatTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(
    navHostController: NavHostController,
    backPressedDispatcher: OnBackPressedDispatcher
) {
    val pagerState = rememberPagerState()
    OnBoardingContent(
        pagerState = pagerState,
        navHostController = navHostController
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingContent(
    pagerState: PagerState,
    navHostController: NavHostController
) {
    val context = LocalContext.current
    val text = buildAnnotatedString {
        append("By joining, you agree to the ")
        pushStringAnnotation(tag = "policy", annotation = "")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("privacy policy")
        }
        pop()
        append(" and ")
        pushStringAnnotation(tag = "terms", annotation = "")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("terms of use")
        }
        pop()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(all = 24.dp)
        ) {
            Text(text = "Welcome to LiveChat")
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ClickableText(text = text, onClick = { offset ->
                    text.getStringAnnotations(tag = "policy", start = offset, end = offset).firstOrNull()
                        ?.let {
                            Toast.makeText(context, "To be implemented yet - Privacy Policy", Toast.LENGTH_SHORT).show()
                        }
                    text.getStringAnnotations(tag = "terms", start = offset, end = offset).firstOrNull()
                        ?.let {
                            Toast.makeText(context, "To be implemented yet - Terms of Service", Toast.LENGTH_SHORT).show()
                        }
                })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun OnBoardingPreview() {
    val pagerState = rememberPagerState()
    val navHostController = rememberNavController()
    LiveChatTheme {
        OnBoardingContent(
            pagerState = pagerState,
            navHostController = navHostController
        )
    }
}