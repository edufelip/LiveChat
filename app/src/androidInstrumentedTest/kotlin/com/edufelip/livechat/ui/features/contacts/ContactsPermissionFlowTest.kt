package com.edufelip.livechat.ui.features.contacts

import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
                    onSearchQueryChange = { query ->
                        setState?.invoke(currentState.copy(searchQuery = query))
                    },
                    onDismissError = {},
                    onBack = {},
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

    @Test
    fun searchFiltersContacts() {
        var primaryName = ""
        var tertiaryName = ""

        composeRule.setContent {
            LiveChatTheme {
                val strings = liveChatStrings()
                val previewContacts = remember(strings) { PreviewFixtures.contacts(strings) }
                val validatedContacts = remember(previewContacts) { previewContacts.filter { it.isRegistered } }
                var state by remember(previewContacts) {
                    mutableStateOf(
                        ContactsUiState(
                            localContacts = previewContacts,
                            validatedContacts = validatedContacts,
                            isLoading = false,
                            isSyncing = false,
                            errorMessage = null,
                        ),
                    )
                }
                SideEffect {
                    primaryName = strings.preview.contactPrimaryName
                    tertiaryName = strings.preview.contactTertiaryName
                }
                ContactsScreen(
                    state = state,
                    showSyncButton = false,
                    onInvite = {},
                    onContactSelected = {},
                    onSync = {},
                    onSearchQueryChange = { query ->
                        state = state.copy(searchQuery = query)
                    },
                    onDismissError = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText(primaryName).assertIsDisplayed()
        composeRule.onNodeWithText(tertiaryName).assertIsDisplayed()

        composeRule.onNodeWithTag(ContactsTestTags.SEARCH_FIELD).performTextInput(primaryName.take(2))
        composeRule.onNodeWithText(primaryName).assertIsDisplayed()
        composeRule.onNodeWithText(tertiaryName).assertDoesNotExist()
    }
}
