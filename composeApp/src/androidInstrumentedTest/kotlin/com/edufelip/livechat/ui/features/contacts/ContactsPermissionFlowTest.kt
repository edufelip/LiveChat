package com.edufelip.livechat.ui.features.contacts

import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ContactsUiState
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.contacts.screens.ContactsScreen
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LiveChatTheme
import org.junit.Rule
import org.junit.Test

class ContactsPermissionFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun firstVisitShowsSyncPrompt_thenLoadsContacts() {
        val initialState =
            ContactsUiState(
                localContacts = emptyList(),
                validatedContacts = emptyList(),
                isLoading = false,
                isSyncing = false,
                errorMessage = null,
            )
        var currentState = initialState
        var setState: ((ContactsUiState) -> Unit)? = null
        var emptyStateLabel = ""
        var syncCta = ""
        var syncingLabel = ""
        var primaryName = ""
        var tertiaryName = ""
        var previewContacts: List<Contact> = emptyList()
        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                val preview = strings.preview
                SideEffect {
                    emptyStateLabel = strings.contacts.emptyState
                    syncCta = strings.contacts.syncCta
                    syncingLabel = strings.contacts.syncing
                    primaryName = preview.contactPrimaryName
                    tertiaryName = preview.contactTertiaryName
                    previewContacts = PreviewFixtures.contacts(strings)
                }
                var state by remember { mutableStateOf(initialState) }
                SideEffect {
                    setState = { newState ->
                        currentState = newState
                        state = newState
                    }
                }
                ContactsScreen(
                    state = state,
                    showSyncButton = true,
                    onInvite = {},
                    onContactSelected = {},
                    onSync = {
                        setState?.invoke(currentState.copy(isSyncing = true))
                    },
                    onDismissError = {},
                )
            }
        }

        composeRule.onNodeWithText(emptyStateLabel).assertIsDisplayed()
        composeRule.onNodeWithText(syncCta).assertIsDisplayed()

        composeRule.onNodeWithText(syncCta).performClick()
        composeRule.onNodeWithText(syncingLabel).assertIsDisplayed()

        composeRule.runOnIdle {
            val validated = previewContacts.filter { it.isRegistered }
            setState?.invoke(
                currentState.copy(
                    localContacts = previewContacts,
                    validatedContacts = validated,
                    isSyncing = false,
                ),
            )
        }

        composeRule.onNodeWithText(primaryName).assertIsDisplayed()
        composeRule.onNodeWithText(tertiaryName).assertIsDisplayed()
    }
}
