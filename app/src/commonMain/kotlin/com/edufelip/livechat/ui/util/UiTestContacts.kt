package com.edufelip.livechat.ui.util

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.resources.LiveChatStrings

fun uiTestContacts(strings: LiveChatStrings): List<Contact> = PreviewFixtures.contacts(strings)

fun uiTestContacts(): List<Contact> = PreviewFixtures.contacts(LiveChatStrings())
