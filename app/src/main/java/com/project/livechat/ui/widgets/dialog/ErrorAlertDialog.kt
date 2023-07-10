package com.project.livechat.ui.widgets.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.livechat.ui.theme.LiveChatTheme

@Composable
fun ErrorAlertDialog(
    title: String,
    description: String,
    setShowDialog: (Boolean) -> Unit = {},
    confirmText: String? = null,
    confirmAction: () -> Unit = {},
    hasDismissButton: Boolean = false
) {
    AlertDialog(
        onDismissRequest = { setShowDialog(false) },
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "icon_error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = description)
            }
        },
        confirmButton = {
            Button(onClick = {
                confirmAction()
                setShowDialog(false)
            }) {
                Text(confirmText ?: "Ok")
            }
        },
        dismissButton = {
            if (hasDismissButton) {
                Button(onClick = {
                    setShowDialog(false)
                }) {
                    Text("Sair")
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
@Preview
fun ErrorAlertDialogPreview() {
    LiveChatTheme {
        ErrorAlertDialog(title = "Error", description = "Couldn't make request")
    }
}