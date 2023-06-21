package com.project.livechat.ui.screens.onboarding.pagerViews

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.livechat.ui.screens.onboarding.OnBoardingViewModel
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.utils.AnnotatedStrStruct
import com.project.livechat.ui.utils.AnnotatedStructType
import com.project.livechat.ui.utils.buildLinkText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingTermsAgreement(
    onBoardingViewModel: OnBoardingViewModel
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
                ClickableText(text = annotatedString, onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = "policy",
                        start = offset,
                        end = offset
                    ).firstOrNull()
                        ?.let {
                            Toast.makeText(
                                context,
                                "To be implemented yet - Privacy Policy",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    annotatedString.getStringAnnotations(
                        tag = "terms",
                        start = offset,
                        end = offset
                    ).firstOrNull()
                        ?.let {
                            Toast.makeText(
                                context,
                                "To be implemented yet - Terms of Service",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                })
            }

            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onClick = {
                    onBoardingViewModel.navigateForward()
                }
            ) {
                Text(text = "Agree and continue")
            }
        }
    }
}

@Preview
@Composable
fun OnBoardingTermsAgreementPreview() {
    val onBoardingViewModel = viewModel<OnBoardingViewModel>()
    LiveChatTheme {
        OnBoardingTermsAgreement(
            onBoardingViewModel = onBoardingViewModel
        )
    }
}