package com.project.livechat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.project.livechat.domain.useCases.GetLocalContactsUseCase
import com.project.livechat.domain.utils.StateUI
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class ContactsViewModel(
    private val checkRegisteredContactsUseCase: CheckRegisteredContactsUseCase,
    private val getLocalContactsUseCase: GetLocalContactsUseCase
) : ViewModel() {

    private val _validatedContactsList: MutableStateFlow<StateUI<List<Contact>>> =
        MutableStateFlow(StateUI.Idle)
    val validatedContactsList = _validatedContactsList.asStateFlow()

    private val _localContactsList: MutableStateFlow<StateUI<List<Contact>>> =
        MutableStateFlow(StateUI.Idle)
    val localContactsList = _localContactsList.asStateFlow()

    fun checkContacts(phoneContacts: List<Contact>) {
        getLocalContacts().invokeOnCompletion {
            checkRegisteredContacts(phoneContacts)
        }
    }

    private fun checkRegisteredContacts(phoneContacts: List<Contact>) {
        val data = (_localContactsList.value as? StateUI.Success<List<Contact>>)?.data ?: return
        viewModelScope.launch {
            checkRegisteredContactsUseCase(phoneContacts, data)
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

    private fun getLocalContacts(): Job {
        return viewModelScope.launch {
            getLocalContactsUseCase()
                .onStart {
                    _localContactsList.value = StateUI.Loading
                }
                .catch {
                    it.printStackTrace()
                    _localContactsList.value = StateUI.Error(cause = it)
                }
                .collect {
                    _localContactsList.value = StateUI.Success(it)
                }
        }
    }
}
