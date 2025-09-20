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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.ContactsUiState
import com.project.livechat.ui.models.ContactUI
import com.project.livechat.ui.models.toContactUI
import com.project.livechat.ui.models.toDomain
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
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by contactsViewModel.uiState.collectAsStateWithLifecycle()
    var phoneContacts by remember { mutableStateOf<List<Contact>>(emptyList()) }

    val permissionsToRequest = arrayOf(Manifest.permission.READ_CONTACTS)
    val dialogQueue = permissionViewModel.visiblePermissionDialogQueue
    val permissionLauncher = getMultiplePermissionsLauncher(
        permissionsToRequest,
        permissionViewModel,
        hashMapOf(
            Manifest.permission.READ_CONTACTS to {
                val contacts = context.getAllContacts()
                phoneContacts = contacts
                contactsViewModel.syncContacts(contacts)
            }
        )
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionLauncher.launch(permissionsToRequest)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ContactsScreenContent(
        navHostController = navHostController,
        uiState = uiState,
        phoneContacts = phoneContacts,
        onRefresh = {
            val contacts = context.getAllContacts()
            phoneContacts = contacts
            contactsViewModel.syncContacts(contacts)
        },
        onInvite = { contactsViewModel.invite(it.toDomain()) }
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
    uiState: ContactsUiState,
    phoneContacts: List<Contact>,
    onRefresh: () -> Unit = {},
    onInvite: (ContactUI) -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    val filterQuery = searchText.trim()
    val registeredPhones = remember(uiState.validatedContacts) {
        uiState.validatedContacts.map { it.phoneNo }.toSet()
    }
    val displayContacts = remember(uiState.localContacts, phoneContacts) {
        (phoneContacts + uiState.localContacts).distinctBy { it.phoneNo }
            .map { it.toContactUI() }
    }
    val filteredContacts = remember(displayContacts, filterQuery) {
        if (filterQuery.isBlank()) displayContacts
        else displayContacts.filter { contact ->
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

            when {
                uiState.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
                uiState.errorMessage != null -> ErrorState(
                    modifier = Modifier.fillMaxSize(),
                    onRetry = onRefresh
                )
                filteredContacts.isEmpty() -> EmptyContactsState(modifier = Modifier.fillMaxSize())
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredContacts, key = { it.phoneNo }) { contact ->
                        val isRegistered = registeredPhones.contains(contact.phoneNo)
                        ContactItem(
                            contact = contact,
                            isRegistered = isRegistered,
                            onInvite = if (isRegistered || uiState.isSyncing) null else { { onInvite(contact) } }
                        )
                    }
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
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Something went wrong")
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}

@Composable
private fun EmptyContactsState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = "No contacts found")
    }
}

@Preview
@Composable
private fun LoadingStatePreview() {
    LoadingState(modifier = Modifier.fillMaxSize())
}
