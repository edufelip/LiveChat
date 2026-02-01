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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.ui.components.atoms.BottomSheetDragHandle
import com.edufelip.livechat.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountDeleteBottomSheet(
    step: DeleteBottomSheetStep,
    confirmTitle: String,
    confirmBody: String,
    confirmCta: String,
    cancelLabel: String,
    farewellTitle: String,
    farewellBody: String,
    countdownCta: String,
    countdownReadyCta: String,
    countdownSeconds: Int,
    reauthTitle: String,
    reauthBody: String,
    reauthCodeLabel: String,
    reauthCodePlaceholder: String,
    reauthCta: String,
    reauthResendLabel: String,
    reauthError: String?,
    otp: String,
    onOtpChange: (String) -> Unit,
    canResend: Boolean,
    onResend: () -> Unit,
    onConfirmDelete: () -> Unit,
    onConfirmCountdown: () -> Unit,
    onConfirmReauth: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    isDeleting: Boolean,
    isVerifying: Boolean,
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
                    ).navigationBarsPadding()
                    .imePadding(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            when (step) {
                DeleteBottomSheetStep.Confirm -> {
                    Text(
                        text = confirmTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = confirmBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))
                    Button(
                        onClick = onConfirmDelete,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                        enabled = !isDeleting,
                        shape = RoundedCornerShape(percent = 50),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    ) {
                        Text(
                            text = confirmCta,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                    TextButton(
                        onClick = onCancel,
                        enabled = !isDeleting,
                    ) {
                        Text(cancelLabel)
                    }
                }

                DeleteBottomSheetStep.Countdown -> {
                    Text(
                        text = farewellTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = farewellBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))
                    val label =
                        if (countdownSeconds <= 0) {
                            countdownReadyCta
                        } else {
                            countdownCta
                        }
                    Button(
                        onClick = onConfirmCountdown,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                        enabled = countdownSeconds <= 0 && !isDeleting,
                        shape = RoundedCornerShape(percent = 50),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                    TextButton(
                        onClick = onCancel,
                        enabled = !isDeleting,
                    ) {
                        Text(cancelLabel)
                    }
                }

                DeleteBottomSheetStep.Reauth -> {
                    Text(
                        text = reauthTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = reauthBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = onOtpChange,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = reauthCodeLabel },
                        placeholder = { Text(reauthCodePlaceholder) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        enabled = !isVerifying,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                    if (!reauthError.isNullOrBlank()) {
                        Text(
                            text = reauthError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))
                    Button(
                        onClick = onConfirmReauth,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                        enabled = otp.length == 6 && !isVerifying,
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
                            text = reauthCta,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                    TextButton(
                        onClick = onResend,
                        enabled = canResend && !isVerifying,
                    ) {
                        Text(reauthResendLabel)
                    }
                    TextButton(
                        onClick = onCancel,
                        enabled = !isVerifying,
                    ) {
                        Text(cancelLabel)
                    }
                }
            }
        }
    }
}

internal enum class DeleteBottomSheetStep {
    Confirm,
    Countdown,
    Reauth,
}
