package com.project.livechat.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.project.livechat.ui.navigation.builder.ContactsScreen
import com.project.livechat.ui.screens.home.models.ConversationItemUiModel
import com.project.livechat.ui.screens.home.models.HomeUiState
import com.project.livechat.ui.screens.home.widgets.ConversationCard
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navHostController: NavHostController,
    viewModel: HomeViewModel = koinViewModel(),
    onConversationSelected: (String) -> Unit,
    onContactsClick: () -> Unit = { navHostController.navigate(ContactsScreen) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onConversationClicked = {
            viewModel.onConversationOpened(it.id)
            onConversationSelected(it.id)
        },
        onPinToggle = { id, pinned -> viewModel.togglePinned(id, pinned) },
        onContactsClick = onContactsClick,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onConversationClicked: (ConversationItemUiModel) -> Unit,
    onPinToggle: (String, Boolean) -> Unit,
    onContactsClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onContactsClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            var searchField by remember(uiState.searchQuery) { mutableStateOf(TextFieldValue(uiState.searchQuery)) }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchField,
                onValueChange = {
                    searchField = it
                    onSearchQueryChange(it.text)
                },
                label = { Text(text = "Search conversations") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.conversations.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No conversations yet", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                else -> {
                    val pinned = uiState.conversations.filter { it.isPinned }
                    val others = uiState.conversations.filterNot { it.isPinned }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (pinned.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Pinned")
                            }
                            items(pinned, key = { it.id }) { item ->
                                ConversationCard(
                                    item = item,
                                    onClick = { onConversationClicked(item) },
                                    onTogglePin = { onPinToggle(item.id, it) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        item {
                            SectionHeader(title = if (pinned.isNotEmpty()) "Others" else "Conversations")
                        }
                        items(others, key = { it.id }) { item ->
                            ConversationCard(
                                item = item,
                                onClick = { onConversationClicked(item) },
                                onTogglePin = { onPinToggle(item.id, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Preview
@Composable
private fun HomeContentPreview() {
    LiveChatTheme {
        HomeContent(
            uiState = HomeUiState(
                conversations = listOf(
                    ConversationItemUiModel(
                        id = "1",
                        title = "Walter White",
                        subtitle = "I'm the one who knocks",
                        timeLabel = "10:21 AM",
                        timestamp = 0L,
                        unreadCount = 2,
                        isPinned = true,
                        avatarUrl = null
                    ),
                    ConversationItemUiModel(
                        id = "2",
                        title = "Skyler",
                        subtitle = "Let's talk tonight",
                        timeLabel = "Yesterday",
                        timestamp = 0L,
                        unreadCount = 0,
                        isPinned = false,
                        avatarUrl = null
                    )
                )
            ),
            onSearchQueryChange = {},
            onConversationClicked = {},
            onPinToggle = { _, _ -> },
            onContactsClick = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
