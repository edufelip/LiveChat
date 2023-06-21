package com.project.livechat.ui.utils

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.project.livechat.ui.viewmodels.PermissionViewModel

@Composable
fun getMultiplePermissionsLauncher(
    permissionsToRequest: Array<String>,
    permissionViewModel: PermissionViewModel,
    permissionSuccessDispatcher: HashMap<String, () -> Unit>
): ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsToRequest.forEach { permission ->
                permissionViewModel.onPermissionResult(
                    permission = permission,
                    isGranted = perms[permission] == true
                )

                permissionSuccessDispatcher[permission]?.invoke()
            }
        }
    )
}