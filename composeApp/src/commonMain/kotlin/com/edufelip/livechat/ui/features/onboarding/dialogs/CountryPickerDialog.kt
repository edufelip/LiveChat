package com.edufelip.livechat.ui.features.onboarding.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.onboarding.CountryOption
import com.edufelip.livechat.ui.features.onboarding.OnboardingTestTags
import com.edufelip.livechat.ui.resources.liveChatStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun CountryPickerDialog(
    currentSelection: CountryOption,
    onDismiss: () -> Unit,
    onSelect: (CountryOption) -> Unit,
) {
    val strings = liveChatStrings().onboarding
    val countries = remember { CountryOption.defaults }
    var query by remember { mutableStateOf("") }
    val filteredCountries =
        remember(query, countries) {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                countries
            } else {
                val needle = trimmed.lowercase()
                countries.filter { option ->
                    option.name.lowercase().contains(needle) ||
                        option.dialCode.contains(needle) ||
                        option.isoCode.lowercase().contains(needle)
                }
            }
        }
    val listState = rememberLazyListState()
    LaunchedEffect(query) {
        listState.scrollToItem(0)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.countryPickerTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(OnboardingTestTags.COUNTRY_PICKER_SEARCH),
                    singleLine = true,
                    placeholder = { Text(strings.countryPickerSearchPlaceholder) },
                )

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = listState,
                ) {
                    if (filteredCountries.isEmpty()) {
                        item {
                            Text(
                                text = strings.countryPickerEmpty,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(filteredCountries, key = { it.isoCode }) { option ->
                            Surface(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .testTag(OnboardingTestTags.COUNTRY_OPTION_PREFIX + option.isoCode)
                                        .clickable { onSelect(option) },
                                tonalElevation = if (option == currentSelection) 4.dp else 0.dp,
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = option.flag + " " + option.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = option.dialCode,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.countryPickerClose)
            }
        },
    )
}

@DevicePreviews
@Preview
@Composable
private fun CountryPickerDialogPreview() {
    LiveChatPreviewContainer {
        CountryPickerDialog(
            currentSelection = CountryOption.default(),
            onDismiss = {},
            onSelect = {},
        )
    }
}
