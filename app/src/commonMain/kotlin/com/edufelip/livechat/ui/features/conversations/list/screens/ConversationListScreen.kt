package com.edufelip.livechat.ui.features.conversations.list.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.domain.models.ConversationFilter
import com.edufelip.livechat.domain.models.ConversationListUiState
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.app.AppIcons
import com.edufelip.livechat.ui.components.molecules.LoadingState
import com.edufelip.livechat.ui.features.conversations.list.components.ConversationListRow
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import androidx.compose.ui.tooling.preview.Preview

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ConversationListScreen(
    state: ConversationListUiState,
    onSearch: (String) -> Unit,
    onConversationSelected: (ConversationSummary) -> Unit,
    onTogglePin: (ConversationSummary, Boolean) -> Unit,
    onToggleMute: (ConversationSummary, Boolean) -> Unit,
    onToggleArchive: (ConversationSummary, Boolean) -> Unit,
    onFilterSelected: (ConversationFilter) -> Unit,
    onCompose: () -> Unit,
    onEmptyStateAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    val conversationStrings = strings.conversation
    val onSearchAction = rememberStableAction(onSearch)
    val onConversationSelectedAction = rememberStableAction(onConversationSelected)
    val onTogglePinAction = rememberStableAction(onTogglePin)
    val onToggleMuteAction = rememberStableAction(onToggleMute)
    val onToggleArchiveAction = rememberStableAction(onToggleArchive)
    val onFilterSelectedAction = rememberStableAction(onFilterSelected)
    val onComposeAction = rememberStableAction(onCompose)
    val onEmptyStateActionState = rememberStableAction(onEmptyStateAction)
    val filterOrder = remember { ConversationFilter.entries.toList() }
    val filterIndex =
        remember(filterOrder) {
            filterOrder.withIndex().associate { entry -> entry.value to entry.index }
        }
    val filterOptions =
        remember(conversationStrings) {
            ConversationFilter.entries.map { filter ->
                val label =
                    when (filter) {
                        ConversationFilter.All -> conversationStrings.filterAll
                        ConversationFilter.Unread -> conversationStrings.filterUnread
                        ConversationFilter.Pinned -> conversationStrings.filterPinned
                        ConversationFilter.Archived -> conversationStrings.filterArchived
                    }
                FilterChipOption(filter = filter, label = label)
            }
        }
    val conversations = remember(state.conversations) { state.conversations.distinctBy { it.conversationId } }
    val listStates = remember { mutableMapOf<ConversationFilter, LazyListState>() }
    val cachedLists = remember { mutableStateMapOf<ConversationFilter, List<ConversationSummary>>() }
    LaunchedEffect(state.selectedFilter, conversations) {
        cachedLists[state.selectedFilter] = conversations
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        ConversationListHeader(
            title = strings.home.chatsTab,
            onCompose = onComposeAction,
            composeContentDescription = conversationStrings.composeAction,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.md),
        )

        ConversationSearchField(
            query = state.searchQuery,
            placeholder = conversationStrings.searchPlaceholder,
            onQueryChange = onSearchAction,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.gutter),
        )

        ConversationFilterChips(
            options = filterOptions,
            selectedFilter = state.selectedFilter,
            onFilterSelected = onFilterSelectedAction,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.spacing.gutter,
                        end = MaterialTheme.spacing.gutter,
                        top = MaterialTheme.spacing.xs,
                        bottom = MaterialTheme.spacing.sm,
                    ),
        )

        Box(
            modifier = Modifier.weight(1f),
        ) {
            when {
                state.isLoading ->
                    LoadingState(
                        message = conversationStrings.loadingList,
                        modifier = Modifier.fillMaxSize(),
                    )

                else ->
                    AnimatedContent(
                        targetState = state.selectedFilter,
                        transitionSpec = {
                            val initialIndex = filterIndex[initialState] ?: 0
                            val targetIndex = filterIndex[targetState] ?: 0
                            if (targetIndex == initialIndex) {
                                fadeIn(animationSpec = tween(0)) togetherWith
                                    fadeOut(animationSpec = tween(0))
                            } else {
                                val direction = if (targetIndex > initialIndex) 1 else -1
                                val slideIn =
                                    slideInHorizontally(animationSpec = tween(320)) { fullWidth ->
                                        direction * (fullWidth / 4)
                                    }
                                val slideOut =
                                    slideOutHorizontally(animationSpec = tween(260)) { fullWidth ->
                                        -direction * (fullWidth / 4)
                                    }
                                (slideIn + fadeIn(animationSpec = tween(200))) togetherWith
                                    (slideOut + fadeOut(animationSpec = tween(200)))
                            }
                        },
                        contentKey = { it },
                        modifier = Modifier.fillMaxSize(),
                        label = "conversation_filter_transition",
                    ) { filter ->
                        val listState =
                            remember(filter) {
                                listStates[filter] ?: LazyListState().also { listStates[filter] = it }
                            }
                        val list =
                            cachedLists[filter]
                                ?: if (filter == state.selectedFilter) conversations else emptyList()
                        if (list.isEmpty()) {
                            ConversationEmptyState(
                                title = conversationStrings.emptyList,
                                subtitle = conversationStrings.emptyListSubtitle,
                                ctaLabel = conversationStrings.emptyListCta,
                                onCtaClick = onEmptyStateActionState,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            val lastIndex = list.lastIndex
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                                contentPadding =
                                    PaddingValues(
                                        start = MaterialTheme.spacing.gutter,
                                        end = MaterialTheme.spacing.gutter,
                                        bottom = MaterialTheme.spacing.xxxl,
                                    ),
                            ) {
                                itemsIndexed(
                                    items = list,
                                    key = { _, item -> item.conversationId },
                                ) { index, summary ->
                                    ConversationListRow(
                                        summary = summary,
                                        currentUserId = state.currentUserId,
                                        onTogglePin = onTogglePinAction,
                                        onToggleMute = onToggleMuteAction,
                                        onToggleArchive = onToggleArchiveAction,
                                        onClick = onConversationSelectedAction,
                                        showDivider = index != lastIndex,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .animateItem(),
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun ConversationListHeader(
    title: String,
    onCompose: () -> Unit,
    composeContentDescription: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        IconButton(
            onClick = onCompose,
            modifier =
                Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape,
                    ),
        ) {
            Icon(
                imageVector = AppIcons.compose,
                contentDescription = composeContentDescription,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ConversationSearchField(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors =
        TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier =
            modifier
                .heightIn(min = 48.dp),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = colors,
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun ConversationFilterChips(
    options: List<FilterChipOption>,
    selectedFilter: ConversationFilter,
    onFilterSelected: (ConversationFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm / 2),
        contentPadding = PaddingValues(vertical = MaterialTheme.spacing.xs),
    ) {
        items(options, key = { it.filter.name }) { option ->
            val isSelected = selectedFilter == option.filter
            val onFilterClick =
                remember(option.filter, onFilterSelected) {
                    { onFilterSelected(option.filter) }
                }
            ConversationFilterChip(
                label = option.label,
                selected = isSelected,
                onClick = onFilterClick,
            )
        }
    }
}

@Composable
private fun ConversationFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    val contentColor =
        if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ConversationEmptyState(
    title: String,
    subtitle: String,
    ctaLabel: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.size(88.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = AppIcons.conversations,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
        Button(
            onClick = onCtaClick,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(ctaLabel)
        }
    }
}

private data class FilterChipOption(
    val filter: ConversationFilter,
    val label: String,
)

@Composable
private fun rememberStableAction(action: () -> Unit): () -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { actionState.value() } }
}

@Composable
private fun <T> rememberStableAction(action: (T) -> Unit): (T) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value -> actionState.value(value) } }
}

@Composable
private fun <T1, T2> rememberStableAction(action: (T1, T2) -> Unit): (T1, T2) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value1, value2 -> actionState.value(value1, value2) } }
}

@DevicePreviews
@Preview
@Composable
private fun ConversationListScreenPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        ConversationListScreen(
            state = PreviewFixtures.conversationListState(strings),
            onSearch = {},
            onConversationSelected = {},
            onTogglePin = { _, _ -> },
            onToggleMute = { _, _ -> },
            onToggleArchive = { _, _ -> },
            onFilterSelected = {},
            onCompose = {},
            onEmptyStateAction = {},
        )
    }
}
