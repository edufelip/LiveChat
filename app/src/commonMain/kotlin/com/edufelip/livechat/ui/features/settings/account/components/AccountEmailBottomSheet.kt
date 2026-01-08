package com.edufelip.livechat.ui.features.settings.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.ui.components.atoms.BottomSheetDragHandle
import com.edufelip.livechat.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountEmailBottomSheet(
    step: EmailBottomSheetStep,
    title: String,
    description: String,
    verifyTitle: String,
    verifyDescription: String,
    email: String,
    placeholder: String,
    sendLabel: String,
    verifyLabel: String,
    changeLabel: String,
    resendLabel: String,
    onEmailChange: (String) -> Unit,
    onSendVerification: () -> Unit,
    onConfirmVerified: () -> Unit,
    onChangeEmail: () -> Unit,
    onResend: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    confirmEnabled: Boolean,
    keyboardOptions: KeyboardOptions,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val fieldShape = RoundedCornerShape(12.dp)
    val fieldContainerColor =
        if (isDarkTheme) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.4f),
        dragHandle = {
            BottomSheetDragHandle(
                width = 48.dp,
                height = 6.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                topPadding = MaterialTheme.spacing.sm,
                bottomPadding = MaterialTheme.spacing.xs,
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.spacing.xl,
                        top = MaterialTheme.spacing.xs,
                        end = MaterialTheme.spacing.xl,
                        bottom = MaterialTheme.spacing.xxxl,
                    )
                    .navigationBarsPadding()
                    .imePadding(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            val headerTitle =
                when (step) {
                    EmailBottomSheetStep.Entry -> title
                    EmailBottomSheetStep.AwaitVerification -> verifyTitle
                }
            val headerDescription =
                when (step) {
                    EmailBottomSheetStep.Entry -> description
                    EmailBottomSheetStep.AwaitVerification -> verifyDescription
                }
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
            if (headerDescription.isNotBlank()) {
                Text(
                    text = headerDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = placeholder },
                placeholder = { Text(placeholder) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                enabled = !isLoading && step == EmailBottomSheetStep.Entry,
                readOnly = step == EmailBottomSheetStep.AwaitVerification,
                keyboardOptions = keyboardOptions,
                shape = fieldShape,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = fieldContainerColor,
                        unfocusedContainerColor = fieldContainerColor,
                        disabledContainerColor = fieldContainerColor,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))
            val primaryLabel =
                when (step) {
                    EmailBottomSheetStep.Entry -> sendLabel
                    EmailBottomSheetStep.AwaitVerification -> verifyLabel
                }
            Button(
                onClick =
                    when (step) {
                        EmailBottomSheetStep.Entry -> onSendVerification
                        EmailBottomSheetStep.AwaitVerification -> onConfirmVerified
                    },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                enabled =
                    when (step) {
                        EmailBottomSheetStep.Entry -> confirmEnabled && !isLoading
                        EmailBottomSheetStep.AwaitVerification -> !isLoading
                    },
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(vertical = 14.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            ) {
                Text(
                    text = primaryLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
            if (step == EmailBottomSheetStep.AwaitVerification) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
                TextButton(
                    onClick = onResend,
                    enabled = !isLoading,
                ) {
                    Text(resendLabel)
                }
                TextButton(
                    onClick = onChangeEmail,
                    enabled = !isLoading,
                ) {
                    Text(changeLabel)
                }
            }
        }
    }
}

internal enum class EmailBottomSheetStep {
    Entry,
    AwaitVerification,
}
