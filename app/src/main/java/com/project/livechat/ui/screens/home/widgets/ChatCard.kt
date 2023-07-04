package com.project.livechat.ui.screens.home.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.project.livechat.R
import com.project.livechat.ui.screens.home.models.ChatCardModel
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.utils.extensions.badgeLayout

@Composable
fun ChatCard(
    chatCardModel: ChatCardModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .size(52.dp)
                .padding(4.dp)
                .clip(CircleShape),
            model = ImageRequest.Builder(LocalContext.current)
                .data(chatCardModel.photo)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_background) // Todo (Change this)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, bottom = 4.dp)
        ) {
            Text(text = chatCardModel.name, style = MaterialTheme.typography.titleSmall)
            Text(
                text = chatCardModel.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Divider(
                modifier = Modifier
                    .height(1.dp)
                    .padding(start = 8.dp, end = 8.dp)
                    .alpha(0.4f)
            )
        }
        Column(
            modifier = Modifier.padding(end = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(text = chatCardModel.lastMessageTime, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chatCardModel.unreadCount.toString(),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                    .badgeLayout(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

}

@Composable
@Preview
fun ChatCardPreview() {
    LiveChatTheme {
        ChatCard(
            ChatCardModel(
                id = 0,
                name = "Walter White",
                lastMessage = "I'm the one who knocks",
                lastMessageTime = "06:43 AM",
                photo = "",
                unreadCount = 3
            )
        )
    }
}