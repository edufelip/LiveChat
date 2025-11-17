package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IContactsRepository

class ApplyContactSyncPlanUseCase(
    private val repository: IContactsRepository,
) {
    suspend operator fun invoke(plan: ContactSyncPlan) {
        if (plan.toRemove.isNotEmpty()) {
            repository.removeContactsFromLocal(plan.toRemove)
        }
        if (plan.toInsert.isNotEmpty()) {
            repository.addContactsToLocal(plan.toInsert)
        }
        if (plan.toUpdate.isNotEmpty()) {
            repository.updateContacts(plan.toUpdate)
        }
    }
}
