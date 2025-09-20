package com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.project.livechat.ui.screens.onboarding.models.NumberVerificationFormState
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.viewmodels.OnBoardingViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun OnBoardingNumberVerification(
    onBoardingViewModel: OnBoardingViewModel,
    onNavigateBack: () -> Unit
) {
    val state = onBoardingViewModel.screenState

    NumberVerificationContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onPhoneCodeChanged = { isoCode, dialCode ->
            onBoardingViewModel.onValidationEvent(
                NumberVerificationFormEvent.PhoneCodeChanged(isoCode, dialCode)
            )
        },
        onPhoneNumberChanged = { number ->
            onBoardingViewModel.onValidationEvent(
                NumberVerificationFormEvent.PhoneNumberChanged(number)
            )
        },
        onSubmit = {
            onBoardingViewModel.onValidationEvent(NumberVerificationFormEvent.Submit)
        }
    )
}

@Composable
private fun NumberVerificationContent(
    state: NumberVerificationFormState,
    onNavigateBack: () -> Unit,
    onPhoneCodeChanged: (isoCode: String, dialCode: String) -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
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

    val sansFont = FontFamily.SansSerif
    val displayFont = FontFamily.Serif

    val scope = rememberCoroutineScope()
    val phoneNumberUtil = remember { PhoneNumberUtil.getInstance() }
    val countryOptions = remember { buildCountryOptions(phoneNumberUtil) }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
    val selectedCountry = remember(state.countryIso, countryOptions) {
        countryOptions.firstOrNull { option ->
            option.isoCode.equals(state.countryIso, ignoreCase = true)
        } ?: countryOptions.first()
    }
    val formattedPhoneNumber = remember(state.countryIso, state.phoneNum) {
        formatPhoneNumber(phoneNumberUtil, state.countryIso, state.phoneNum)
    }

    LaunchedEffect(selectedCountry) {
        if (
            !selectedCountry.isoCode.equals(state.countryIso, ignoreCase = true) ||
            selectedCountry.dialCode != state.phoneCode
        ) {
            onPhoneCodeChanged(selectedCountry.isoCode, selectedCountry.dialCode)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Enter your phone number",
                    color = textColor,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = displayFont,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "We'll send you a code to verify your number.",
                    color = textColor.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = sansFont
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))

                PhoneInputRow(
                    modifier = Modifier.fillMaxWidth(),
                    textColor = textColor,
                    inputBackground = inputBackground,
                    selectedCountry = selectedCountry,
                    countryOptions = countryOptions,
                    dropdownExpanded = dropdownExpanded,
                    onOpenDropdown = { dropdownExpanded = true },
                    onDismissDropdown = { dropdownExpanded = false },
                    onCountrySelected = { option ->
                        dropdownExpanded = false
                        onPhoneCodeChanged(option.isoCode, option.dialCode)
                    },
                    formattedNumber = formattedPhoneNumber,
                    onPhoneNumberChanged = onPhoneNumberChanged
                )

                if (state.phoneNumError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.phoneNumError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Start
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                StepIndicators(
                    totalSteps = state.totalPages,
                    currentStep = state.currentPage,
                    activeColor = primaryColor,
                    inactiveColor = textColor.copy(alpha = 0.3f)
                )

                Button(
                    onClick = {
                        scope.launch {
                            focusManager.clearFocus()
                            onSubmit()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = textColor
                    )
                ) {
                    Text(
                        text = "Continue",
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

@Composable
private fun PhoneInputRow(
    modifier: Modifier,
    textColor: Color,
    inputBackground: Color,
    selectedCountry: CountryOption,
    countryOptions: List<CountryOption>,
    dropdownExpanded: Boolean,
    onOpenDropdown: () -> Unit,
    onDismissDropdown: () -> Unit,
    onCountrySelected: (CountryOption) -> Unit,
    formattedNumber: String,
    onPhoneNumberChanged: (String) -> Unit
) {
    val borderColor = textColor.copy(alpha = 0.2f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .background(inputBackground, RoundedCornerShape(16.dp))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onOpenDropdown)
                    .padding(horizontal = 12.dp, vertical = 14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${selectedCountry.flagEmoji} +${selectedCountry.dialCode}",
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    tint = textColor
                )
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = onDismissDropdown,
                modifier = Modifier
                    .width(240.dp)
                    .heightIn(max = 320.dp)
            ) {
                LazyColumn {
                    items(countryOptions, key = { it.isoCode }) { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${option.flagEmoji} ${option.displayName} (+${option.dialCode})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = { onCountrySelected(option) }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = formattedNumber,
            onValueChange = { input ->
                onPhoneNumberChanged(input.filter(Char::isDigit).take(15))
            },
            modifier = Modifier
                .weight(1f)
                .background(inputBackground, RoundedCornerShape(16.dp)),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                color = textColor
            ),
            placeholder = {
                Text(
                    text = "Phone number",
                    color = textColor.copy(alpha = 0.5f)
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = inputBackground,
                unfocusedContainerColor = inputBackground,
                focusedIndicatorColor = textColor.copy(alpha = 0.4f),
                unfocusedIndicatorColor = textColor.copy(alpha = 0.2f),
                cursorColor = textColor
            )
        )
    }
}

@Composable
fun StepIndicators(
    totalSteps: Int,
    currentStep: Int,
    activeColor: Color,
    inactiveColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentStep) 12.dp else 8.dp)
                    .background(
                        color = if (index == currentStep) activeColor else inactiveColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

private data class CountryOption(
    val isoCode: String,
    val dialCode: String,
    val displayName: String,
    val flagEmoji: String
)

private fun buildCountryOptions(
    phoneNumberUtil: PhoneNumberUtil
): List<CountryOption> {
    return phoneNumberUtil.supportedRegions
        .mapNotNull { iso ->
            val dialCode = phoneNumberUtil.getCountryCodeForRegion(iso)
            if (dialCode == 0) return@mapNotNull null
            val displayName = Locale("", iso).displayCountry.takeIf { it.isNotBlank() } ?: iso
            CountryOption(
                isoCode = iso.uppercase(),
                dialCode = dialCode.toString(),
                displayName = displayName,
                flagEmoji = iso.uppercase().toFlagEmoji()
            )
        }
        .sortedBy { it.displayName }
}

private fun formatPhoneNumber(
    phoneNumberUtil: PhoneNumberUtil,
    isoCode: String,
    digits: String
): String {
    if (digits.isBlank()) return ""
    val region = isoCode.takeIf { it.isNotBlank() }?.uppercase() ?: "US"
    return try {
        val formatter = phoneNumberUtil.getAsYouTypeFormatter(region)
        var formatted = ""
        digits.forEach { char ->
            formatted = formatter.inputDigit(char)
        }
        formatted
    } catch (error: Exception) {
        digits
    }
}

private fun String.toFlagEmoji(): String {
    if (length != 2) return "🏳️"
    val upper = uppercase(Locale.getDefault())
    val base = 0x1F1E6 - 'A'.code
    val firstFlag = base + upper[0].code
    val secondFlag = base + upper[1].code
    return String(Character.toChars(firstFlag)) + String(Character.toChars(secondFlag))
}

@Preview(showBackground = true)
@Composable
private fun OnBoardingNumberVerificationPreview() {
    LiveChatTheme {
        NumberVerificationContent(
            state = NumberVerificationFormState(currentPage = 0, totalPages = 3, countryIso = "US"),
            onNavigateBack = {},
            onPhoneCodeChanged = { _, _ -> },
            onPhoneNumberChanged = {},
            onSubmit = {}
        )
    }
}
