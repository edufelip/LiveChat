package com.project.livechat.ui.screens.contacts

import android.Manifest
import android.app.Activity
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.project.livechat.domain.models.Contact
import com.project.livechat.ui.screens.contacts.widgets.ContactItem
import com.project.livechat.ui.utils.getAllContacts
import com.project.livechat.ui.utils.getMultiplePermissionsLauncher
import com.project.livechat.ui.utils.openAppSettings
import com.project.livechat.ui.viewmodels.ContactsViewModel
import com.project.livechat.ui.viewmodels.PermissionViewModel
import com.project.livechat.ui.widgets.ContactsPermissionTextProvider
import com.project.livechat.ui.widgets.PermissionDialog

@Composable
fun ContactsScreen(
    navHostController: NavHostController,
    backPressedDispatcher: OnBackPressedDispatcher,
    permissionViewModel: PermissionViewModel,
    contactsViewModel: ContactsViewModel
) {
    val context = LocalContext.current
    val activity = context as Activity

    val contactsList = remember { mutableStateOf(listOf<Contact>()) }

    val permissionsToRequest = arrayOf(Manifest.permission.READ_CONTACTS)
    val dialogQueue = permissionViewModel.visiblePermissionDialogQueue
    val multiplePermissionResultLauncher = getMultiplePermissionsLauncher(
        permissionsToRequest,
        permissionViewModel,
        hashMapOf(
            Manifest.permission.READ_CONTACTS to {
                contactsList.value = context.getAllContacts()
                // Pegar lista de contatos do Room
                // Checar com a lista do device
                // Pegar a diferenca e verificar com o Firebase
                // O que tiver a mais adiciona no Room, o que tiver de menos remove
            }
        )
    )
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val eventObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    multiplePermissionResultLauncher.launch(permissionsToRequest)
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(eventObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(eventObserver)
        }
    })

    ContactsScreenContent(
        navHostController = navHostController,
        contactsList = contactsList.value
    )

    dialogQueue.reversed().forEach { permission ->
        PermissionDialog(
            permissionTextProvider = when (permission) {
                Manifest.permission.READ_CONTACTS -> {
                    ContactsPermissionTextProvider()
                }

                else -> return@forEach
            },
            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                activity,
                permission
            ),
            onDismiss = permissionViewModel::dismissDialog,
            onOkClick = {
                permissionViewModel.dismissDialog()
            },
            onGoToAppSettingsClick = {
                activity.openAppSettings()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreenContent(
    navHostController: NavHostController? = null,
    contactsList: List<Contact>,
) {
    val searchText = remember {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Contacts")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navHostController?.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize()
        ) {
            TextField(
                value = searchText.value,
                onValueChange = {
                    searchText.value = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Search") }
            )
            LazyColumn() {
                itemsIndexed(items = contactsList) { _, contact ->
                    ContactItem(contact = contact)
                }
            }
        }
    }
}

@Composable
@Preview
fun ContactsScreenPreview() {
    ContactsScreenContent(
        contactsList = listOf(
            Contact(
                name = "Reginaldo",
                phoneNo = "+5521985670564",
                description = "A very nice dude 😘",
                photo = null
            ),
            Contact(
                name = "Reginaldo",
                phoneNo = "+5521985670564",
                description = "A very nice dude 😘",
                photo = null
            ),
        )
    )
}