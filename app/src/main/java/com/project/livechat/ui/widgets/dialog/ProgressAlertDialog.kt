package com.project.livechat.ui.widgets.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgressAlertDialog(
    title: String,
    description: String,
    setShowDialog: (Boolean) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = { setShowDialog(false) },
        title = { Text(text = title, fontSize = 20.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = description)
                CircularProgressIndicator(modifier = Modifier
                    .size(32.dp)
                    .padding(vertical = 24.dp))
            }
        },
        confirmButton = {},
        modifier = Modifier.fillMaxWidth()
    )
}