package com.project.livechat.ui.screens.contacts

import android.Manifest
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.utils.StateUI
import com.project.livechat.ui.models.ContactUI
import com.project.livechat.ui.models.toContactUI
import com.project.livechat.ui.screens.contacts.widgets.ContactItem
import com.project.livechat.ui.utils.extensions.getAllContacts
import com.project.livechat.ui.utils.extensions.openAppSettings
import com.project.livechat.ui.utils.getMultiplePermissionsLauncher
import com.project.livechat.ui.viewmodels.ContactsViewModel
import com.project.livechat.ui.viewmodels.PermissionViewModel
import com.project.livechat.ui.widgets.ContactsPermissionTextProvider
import com.project.livechat.ui.widgets.PermissionDialog

@Composable
fun ContactsScreen(
    navHostController: NavHostController,
    permissionViewModel: PermissionViewModel,
    contactsViewModel: ContactsViewModel
) {
    val context = LocalContext.current
    val activity = context as Activity

    val localContactsState by contactsViewModel.localContactsList.collectAsStateWithLifecycle()
    val validatedContactsState by contactsViewModel.validatedContactsList.collectAsStateWithLifecycle()
    val contactsState = deriveContactsState(localContactsState, validatedContactsState)

    val permissionsToRequest = arrayOf(Manifest.permission.READ_CONTACTS)
    val dialogQueue = permissionViewModel.visiblePermissionDialogQueue
    val multiplePermissionResultLauncher = getMultiplePermissionsLauncher(
        permissionsToRequest,
        permissionViewModel,
        hashMapOf(
            Manifest.permission.READ_CONTACTS to {
                val contacts = context.getAllContacts()
                contactsViewModel.checkContacts(contacts)
            }
        )
    )
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val eventObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                multiplePermissionResultLauncher.launch(permissionsToRequest)
            }
        }
        lifecycleOwner.lifecycle.addObserver(eventObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(eventObserver)
        }
    }

    ContactsScreenContent(
        navHostController = navHostController,
        contactsState = contactsState,
        onRetry = {
            val contacts = context.getAllContacts()
            contactsViewModel.checkContacts(contacts)
        }
    )

    dialogQueue.reversed().forEach { permission ->
        PermissionDialog(
            permissionTextProvider = when (permission) {
                Manifest.permission.READ_CONTACTS -> ContactsPermissionTextProvider()
                else -> return@forEach
            },
            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(activity, permission),
            onDismiss = permissionViewModel::dismissDialog,
            onOkClick = permissionViewModel::dismissDialog,
            onGoToAppSettingsClick = activity::openAppSettings
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreenContent(
    navHostController: NavHostController? = null,
    contactsState: StateUI<List<ContactUI>>,
    onRetry: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    val filterQuery = searchText.trim()
    val filteredContacts = remember(contactsState, filterQuery) {
        val base = (contactsState as? StateUI.Success)?.data ?: emptyList()
        if (filterQuery.isBlank()) base
        else base.filter { contact ->
            contact.name.contains(filterQuery, ignoreCase = true) ||
                contact.phoneNo.contains(filterQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts") },
                navigationIcon = {
                    IconButton(onClick = { navHostController?.popBackStack() }) {
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
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text(text = "Search") }
            )

            when (contactsState) {
                is StateUI.Success -> {
                    if (filteredContacts.isEmpty()) {
                        EmptyContactsState(modifier = Modifier.fillMaxSize())
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(
                                items = filteredContacts,
                                key = { _, contact -> contact.id }
                            ) { _, contact ->
                                ContactItem(contact = contact)
                            }
                        }
                    }
                }

                is StateUI.Error -> {
                    ErrorState(
                        modifier = Modifier.fillMaxSize(),
                        onRetry = onRetry
                    )
                }

                StateUI.Loading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }

                StateUI.Idle -> {
                    IdleState(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "We couldn't reach Firestore.")
            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}

@Composable
private fun EmptyContactsState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = "No contacts to show yet.")
    }
}

@Composable
private fun IdleState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = "Grant contacts permission to load your list.")
    }
}

@Composable
@Preview
fun ContactsScreenPreview() {
    ContactsScreenContent(
        contactsState = StateUI.Success(
            listOf(
                Contact(
                    id = 1,
                    name = "Reginaldo",
                    phoneNo = "+5521985670564",
                    description = "A very nice dude 😘",
                    photo = null
                ).toContactUI(),
                Contact(
                    id = 2,
                    name = "Maria",
                    phoneNo = "+5521985000000",
                    description = null,
                    photo = null
                ).toContactUI()
            )
        )
    )
}

private fun deriveContactsState(
    localState: StateUI<List<Contact>>,
    remoteState: StateUI<List<Contact>>
): StateUI<List<ContactUI>> {
    val localUi = localState.mapState { contacts -> contacts.toUiModels() }
    val remoteUi = remoteState.mapState { contacts -> contacts.toUiModels() }

    return when (remoteUi) {
        is StateUI.Success -> if (remoteUi.data.isEmpty()) localUi else remoteUi
        is StateUI.Error -> when (localUi) {
            is StateUI.Success -> localUi
            else -> remoteUi
        }
        StateUI.Loading -> when (localUi) {
            is StateUI.Success -> localUi
            is StateUI.Error -> localUi
            StateUI.Loading -> StateUI.Loading
            StateUI.Idle -> StateUI.Loading
        }
        StateUI.Idle -> localUi
    }
}

private inline fun <T, R> StateUI<T>.mapState(crossinline transform: (T) -> R): StateUI<R> = when (this) {
    is StateUI.Success -> StateUI.Success(transform(data))
    is StateUI.Error -> StateUI.Error(type, cause)
    StateUI.Loading -> StateUI.Loading
    StateUI.Idle -> StateUI.Idle
}

private fun List<Contact>.toUiModels(): List<ContactUI> = map { it.toContactUI() }
