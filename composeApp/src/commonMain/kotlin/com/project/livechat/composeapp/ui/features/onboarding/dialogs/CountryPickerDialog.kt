package com.project.livechat.composeapp.ui.features.onboarding.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.ui.features.onboarding.CountryOption
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun CountryPickerDialog(
    currentSelection: CountryOption,
    onDismiss: () -> Unit,
    onSelect: (CountryOption) -> Unit,
) {
    val countries = remember { CountryOption.defaults }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select your country") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                countries.forEach { option ->
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
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
