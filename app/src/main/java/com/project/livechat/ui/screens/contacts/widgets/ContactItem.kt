package com.project.livechat.ui.screens.contacts.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.project.livechat.R
import com.project.livechat.domain.models.Contact
import com.project.livechat.ui.models.ContactUI
import com.project.livechat.ui.models.toContactUI

@Composable
fun ContactItem(
    contact: ContactUI,
    modifier: Modifier = Modifier
) {
    Column {
        Row(
            modifier = modifier
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                .fillMaxWidth()
                .height(52.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(52.dp)
                    .padding(4.dp)
                    .clip(CircleShape),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(contact.photo)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_background) // Todo (Change this)
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = contact.name)
                Text(text = contact.description ?: "")
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            thickness = 1.dp
        )
    }
}

@Preview
@Composable
fun ContactItemPreview() {
    ContactItem(
        contact = Contact(
            id = 1,
            name = "Reginaldo",
            phoneNo = "+5521985670564",
            description = "A very nice dude (I think)",
            photo = null
        ).toContactUI(), modifier = Modifier.background(MaterialTheme.colorScheme.background)
    )
}
