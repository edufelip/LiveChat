package com.project.livechat.ui.screens.onboarding.pagerViews.termsAgreement

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.utils.extensions.AnnotatedStrStruct
import com.project.livechat.ui.utils.extensions.AnnotatedStructType
import com.project.livechat.ui.utils.extensions.buildLinkText
import com.project.livechat.ui.viewmodels.OnBoardingViewModel

@Composable
fun OnBoardingTermsAgreement(
    onBoardingViewModel: OnBoardingViewModel = hiltViewModel()
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

    TermsAgreementContent(
        context = context,
        annotatedString = annotatedString
    ) {
        onBoardingViewModel.navigateForward()
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAgreementContent(
    context: Context,
    annotatedString: AnnotatedString,
    navigateForward: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(all = 24.dp)
        ) {
            Text(text = "Welcome to LiveChat", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
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
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onClick = {
                    navigateForward.invoke()
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
    LiveChatTheme {
        TermsAgreementContent(
            context = LocalContext.current,
            annotatedString = buildLinkText(
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
        ) {}
    }
}