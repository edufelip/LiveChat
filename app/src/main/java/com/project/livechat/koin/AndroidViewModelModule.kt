package com.project.livechat.koin

import androidx.lifecycle.SavedStateHandle
import com.project.livechat.ui.viewmodels.ContactsViewModel
import com.project.livechat.ui.viewmodels.HomeViewModel
import com.project.livechat.ui.viewmodels.OnBoardingViewModel
import com.project.livechat.ui.viewmodels.PermissionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val androidViewModelModule = module {
    viewModel { ContactsViewModel(get()) }
    viewModel { PermissionViewModel() }
    viewModel { HomeViewModel(get()) }
    viewModel { (stateHandle: SavedStateHandle) -> OnBoardingViewModel(stateHandle, get(), get()) }
}
