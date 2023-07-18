package com.project.livechat.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.project.livechat.domain.utils.StateUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    val checkRegisteredContactsUseCase: CheckRegisteredContactsUseCase
) : ViewModel() {

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _validatedContactsList: MutableStateFlow<StateUI<List<Contact>>> =
        MutableStateFlow(StateUI.Idle)
    val validatedContactsList = _validatedContactsList.asStateFlow()

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeLast()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted) {
            visiblePermissionDialogQueue.add(0, permission)
        }
    }

    fun checkContacts(phoneContacts: List<Contact>) {
        viewModelScope.launch {
            checkRegisteredContactsUseCase(phoneContacts)
                .onStart {
                    _validatedContactsList.value = StateUI.Loading
                }
                .catch {
                    it.printStackTrace()
                    _validatedContactsList.value = StateUI.Error(cause = it)
                }
                .collect {
                    _validatedContactsList.update { list ->
                        val currentList = (list as? StateUI.Success)?.data ?: emptyList()
                        StateUI.Success(currentList.plus(listOf(it)))
                    }
                }
        }
    }
}