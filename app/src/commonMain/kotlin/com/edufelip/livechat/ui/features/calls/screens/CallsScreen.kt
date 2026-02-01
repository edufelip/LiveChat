package com.edufelip.livechat.ui.features.calls.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallMade
import androidx.compose.material.icons.rounded.CallMissed
import androidx.compose.material.icons.rounded.CallReceived
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.app.AppIcons
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing

@Composable
fun CallsScreen(modifier: Modifier = Modifier) {
    val strings = liveChatStrings().calls
    val isPreview = LocalInspectionMode.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(CallsFilter.All) }
    val filterOptions =
        remember(strings) {
            listOf(
                FilterOption(CallsFilter.All, strings.filterAll),
                FilterOption(CallsFilter.Missed, strings.filterMissed),
            )
        }
    val previewCalls =
        remember {
            buildPreviewCalls()
        }
    val calls = if (isPreview) previewCalls else emptyList()
    val filteredCalls =
        remember(calls, selectedFilter) {
            when (selectedFilter) {
                CallsFilter.All -> calls
                CallsFilter.Missed -> calls.filter { it.isMissed }
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.sm),
        ) {
            Text(
                text = strings.screenTitle,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
            CallsSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = strings.searchPlaceholder,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            CallsSegmentedControl(
                options = filterOptions,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Box(
            modifier = Modifier.weight(1f),
        ) {
            if (filteredCalls.isEmpty()) {
                CallsEmptyState(
                    title = strings.emptyTitle,
                    subtitle = strings.emptySubtitle,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                CallsList(
                    calls = filteredCalls,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun CallsSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
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
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier =
            modifier
                .heightIn(min = 44.dp),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = colors,
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun CallsSegmentedControl(
    options: List<FilterOption>,
    selectedFilter: CallsFilter,
    onFilterSelected: (CallsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            options.forEach { option ->
                val isSelected = option.filter == selectedFilter
                val containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        Color.Transparent
                    }
                val contentColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                Surface(
                    color = containerColor,
                    contentColor = contentColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onFilterSelected(option.filter) },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallsList(
    calls: List<CallEntry>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding =
            PaddingValues(
                start = MaterialTheme.spacing.gutter,
                end = MaterialTheme.spacing.gutter,
                bottom = MaterialTheme.spacing.xxxl,
            ),
    ) {
        itemsIndexed(
            items = calls,
            key = { _, item -> item.id },
        ) { index, call ->
            CallRow(
                call = call,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.sm),
            )
            if (index != calls.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CallRow(
    call: CallEntry,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val titleColor = if (call.isMissed) colors.error else colors.onSurface
    val detailColor = colors.onSurfaceVariant

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CallAvatar(
            avatar = call.avatar,
            modifier = Modifier.size(44.dp),
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.md))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = call.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
                Text(
                    text = call.timeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = detailColor,
                    maxLines = 1,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = call.type.icon,
                    contentDescription = null,
                    tint = if (call.isMissed) colors.error else detailColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = call.detailLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = detailColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = colors.primary,
            )
        }
    }
}

@Composable
private fun CallAvatar(
    avatar: CallAvatar,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = CircleShape,
        color = avatar.backgroundColor,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = avatar.initials,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun CallsEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .padding(horizontal = MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CallsEmptyIllustration(
            modifier = Modifier.fillMaxWidth(0.8f),
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CallsEmptyIllustration(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val primary = colors.primary
    val secondary = colors.secondary
    val tertiary = colors.tertiary
    val surface = colors.surface
    val onSurface = colors.onSurface

    Box(
        modifier = modifier.aspectRatio(4f / 3f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(220.dp)
                    .blur(36.dp)
                    .background(primary.copy(alpha = 0.12f), CircleShape),
        )
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val phoneWidth = size.width * 0.4f
            val phoneHeight = size.height * 0.62f
            val phoneTopLeft =
                Offset(
                    x = (size.width - phoneWidth) / 2f,
                    y = size.height * 0.2f,
                )
            val phoneCorner = 24.dp.toPx()
            drawRoundRect(
                color = surface,
                topLeft = phoneTopLeft,
                size = Size(phoneWidth, phoneHeight),
                cornerRadius = CornerRadius(phoneCorner, phoneCorner),
            )
            val screenInset = 10.dp.toPx()
            val screenCorner = (phoneCorner - 8.dp.toPx()).coerceAtLeast(0f)
            drawRoundRect(
                color = primary.copy(alpha = 0.08f),
                topLeft = phoneTopLeft + Offset(screenInset, screenInset),
                size =
                    Size(
                        phoneWidth - screenInset * 2,
                        phoneHeight - screenInset * 2,
                    ),
                cornerRadius = CornerRadius(screenCorner, screenCorner),
            )

            val cardCorner = 16.dp.toPx()
            drawRoundRect(
                color = secondary.copy(alpha = 0.16f),
                topLeft =
                    Offset(
                        x = size.width * 0.12f,
                        y = size.height * 0.3f,
                    ),
                size =
                    Size(
                        size.width * 0.28f,
                        size.height * 0.14f,
                    ),
                cornerRadius = CornerRadius(cardCorner, cardCorner),
            )
            drawRoundRect(
                color = tertiary.copy(alpha = 0.18f),
                topLeft =
                    Offset(
                        x = size.width * 0.6f,
                        y = size.height * 0.56f,
                    ),
                size =
                    Size(
                        size.width * 0.26f,
                        size.height * 0.12f,
                    ),
                cornerRadius = CornerRadius(cardCorner, cardCorner),
            )

            drawCircle(
                color = onSurface.copy(alpha = 0.06f),
                radius = 5.dp.toPx(),
                center = Offset(size.width * 0.18f, size.height * 0.62f),
            )
            drawCircle(
                color = onSurface.copy(alpha = 0.06f),
                radius = 4.dp.toPx(),
                center = Offset(size.width * 0.82f, size.height * 0.28f),
            )
            drawCircle(
                color = primary.copy(alpha = 0.2f),
                radius = 56.dp.toPx(),
                center = Offset(size.width * 0.2f, size.height * 0.62f),
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        Surface(
            color = primary.copy(alpha = 0.16f),
            shape = CircleShape,
            modifier = Modifier.size(64.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = AppIcons.calls,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

private enum class CallsFilter {
    All,
    Missed,
}

private data class FilterOption(
    val filter: CallsFilter,
    val label: String,
)

private data class CallEntry(
    val id: String,
    val name: String,
    val timeLabel: String,
    val detailLabel: String,
    val type: CallType,
    val isMissed: Boolean,
    val avatar: CallAvatar,
)

private data class CallAvatar(
    val initials: String,
    val backgroundColor: Color,
)

private enum class CallType(
    val icon: ImageVector,
) {
    Outgoing(Icons.Rounded.CallMade),
    Incoming(Icons.Rounded.CallReceived),
    Missed(Icons.Rounded.CallMissed),
    Video(Icons.Rounded.Videocam),
}

private fun buildPreviewCalls(): List<CallEntry> {
    val palette =
        listOf(
            Color(0xFF5C6BC0),
            Color(0xFF26A69A),
            Color(0xFF8E24AA),
            Color(0xFF546E7A),
        )
    return listOf(
        CallEntry(
            id = "call-1",
            name = "Alice Morgan",
            timeLabel = "10:45 AM",
            detailLabel = "Mobile • 5 mins",
            type = CallType.Outgoing,
            isMissed = false,
            avatar = CallAvatar(initials = "AM", backgroundColor = palette[0]),
        ),
        CallEntry(
            id = "call-2",
            name = "David Kim",
            timeLabel = "Yesterday",
            detailLabel = "FaceTime Audio",
            type = CallType.Missed,
            isMissed = true,
            avatar = CallAvatar(initials = "DK", backgroundColor = palette[1]),
        ),
        CallEntry(
            id = "call-3",
            name = "John Smith",
            timeLabel = "Sunday",
            detailLabel = "WhatsApp Audio • 12 mins",
            type = CallType.Incoming,
            isMissed = false,
            avatar = CallAvatar(initials = "JS", backgroundColor = palette[2]),
        ),
        CallEntry(
            id = "call-4",
            name = "Emma Wilson",
            timeLabel = "Friday",
            detailLabel = "FaceTime Video • 24 mins",
            type = CallType.Video,
            isMissed = false,
            avatar = CallAvatar(initials = "EW", backgroundColor = palette[3]),
        ),
    )
}

@DevicePreviews
@Preview
@Composable
private fun CallsScreenPreview() {
    LiveChatPreviewContainer {
        CallsScreen()
    }
}
