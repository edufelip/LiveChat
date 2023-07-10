package com.project.livechat.ui.screens.onboarding

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.project.livechat.R
import com.project.livechat.domain.utils.StateUI
import com.project.livechat.domain.validators.ValidationResult
import com.project.livechat.ui.navigation.Routes
import com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification.OnBoardingNumberVerification
import com.project.livechat.ui.screens.onboarding.pagerViews.oneTimePassword.OnBoardingOneTimePassword
import com.project.livechat.ui.screens.onboarding.pagerViews.oneTimePassword.OneTimePasswordErrors
import com.project.livechat.ui.screens.onboarding.pagerViews.termsAgreement.OnBoardingTermsAgreement
import com.project.livechat.ui.viewmodels.OnBoardingViewModel
import com.project.livechat.ui.widgets.dialog.ErrorAlertDialog
import com.project.livechat.ui.widgets.dialog.ProgressAlertDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(
    navHostController: NavHostController,
    onBoardingViewModel: OnBoardingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context as Activity
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val totalPages = onBoardingViewModel.screenState.totalPages
    val currentPage = onBoardingViewModel.screenState.currentPage
    val sendSmsStateUI = onBoardingViewModel.sendSmsStateUI.collectAsStateWithLifecycle().value
    val verifyCodeStateUI =
        onBoardingViewModel.verifyCodeStateUI.collectAsStateWithLifecycle().value

    var progressTitle: String = ""
    var progressDescription: String = ""

    val showProgressDialog = remember { mutableStateOf(false) }
    val showErrorDialog = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = sendSmsStateUI) {
        showErrorDialog.value = sendSmsStateUI.isError()
        showProgressDialog.value = sendSmsStateUI.isLoading()

        when (sendSmsStateUI) {
            is StateUI.Success -> {
                if (onBoardingViewModel.screenState.currentPage == 1)
                    onBoardingViewModel.navigateForward()
            }

            StateUI.Loading -> {
                progressTitle = "Enviando SMS"
                progressDescription =
                    "Aguarde enquanto enviamos um SMS para que você possa confirmar seu número"
            }

            else -> Unit
        }
    }

    LaunchedEffect(key1 = verifyCodeStateUI) {
        showProgressDialog.value = verifyCodeStateUI.isLoading()
        showErrorDialog.value = verifyCodeStateUI.isError()

        when (verifyCodeStateUI) {
            is StateUI.Success -> {
                Routes.HomeRoute.navigate(navHostController)
            }

            StateUI.Loading -> {
                progressTitle = "Verificando"
                progressDescription = "Aguarde um momento, por favor!"
            }

            else -> Unit
        }
    }

    if (showProgressDialog.value) {
        ProgressAlertDialog(
            title = progressTitle,
            description = progressDescription
        )
    }

    if (showErrorDialog.value) {
        val source = listOf(
            sendSmsStateUI,
            verifyCodeStateUI
        ).firstOrNull { it is StateUI.Error }

        (source as? StateUI.Error).takeIf {
            it?.type != OneTimePasswordErrors.TIME_OUT
        }?.let {
            val errorRes = when (it.type as? OneTimePasswordErrors) {
                OneTimePasswordErrors.INVALID_CREDENTIALS -> R.string.app_name
                OneTimePasswordErrors.SMS_QUOTA_EXCEEDED -> R.string.app_name
                OneTimePasswordErrors.NULL_ACTIVITY -> R.string.app_name
                OneTimePasswordErrors.GENERIC_ERROR -> R.string.app_name
                else -> R.string.generic_error_message
            }

            ErrorAlertDialog(
                title = "Error",
                description = stringResource(id = errorRes),
                confirmAction = {
                    showErrorDialog.value = false
                })
        }
    }

    LaunchedEffect(key1 = context) {
        onBoardingViewModel.validationEvents.collect { event ->
            when (event) {
                is ValidationResult.Success -> {
                    onBoardingViewModel.callSmsVerification(
                        activity = activity,
                        callbacks = onBoardingViewModel.callbacks
                    )
                }

                is ValidationResult.Error -> {
                    val errorMessage = when (event.errorType) {
                        OnBoardingValidationErrors.INVALID_NUMBER ->
                            context.getString(R.string.on_boarding_invalid_number_error)
                    }
                    onBoardingViewModel.parseOnBoardingError(
                        message = errorMessage,
                        errorType = event.errorType
                    )
                }

                is ValidationResult.Idle -> Unit
            }
        }
    }

    LaunchedEffect(key1 = currentPage) {
        scope.launch {
            pagerState.animateScrollToPage(currentPage)
            Log.w("NAVIGATING TO PAGE", currentPage.toString())
        }
    }

    OnBoardingContent(
        pagerState = pagerState,
        totalPages = totalPages
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingContent(
    pagerState: PagerState,
    totalPages: Int
) {
    HorizontalPager(
        pageCount = totalPages,
        state = pagerState,
        userScrollEnabled = false
    ) { index ->
        when (index) {
            0 -> OnBoardingTermsAgreement()
            1 -> OnBoardingNumberVerification()
            2 -> OnBoardingOneTimePassword()
        }
    }
}