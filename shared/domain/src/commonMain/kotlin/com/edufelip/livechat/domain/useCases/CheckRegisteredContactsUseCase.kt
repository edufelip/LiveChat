package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge

class CheckRegisteredContactsUseCase(
    private val buildContactSyncPlan: BuildContactSyncPlanUseCase,
    private val applyContactSyncPlan: ApplyContactSyncPlanUseCase,
    private val validateContactsUseCase: ValidateContactsUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    suspend operator fun invoke(
        phoneContacts: List<Contact>,
        localDbContacts: List<Contact>,
    ): Flow<Contact> {
        val plan = buildContactSyncPlan(phoneContacts, localDbContacts)
        applyContactSyncPlan(plan)

        val alreadyCheckedFlow = flow { plan.alreadyRegistered.forEach { emit(it) } }
        val validationFlow = validateContactsUseCase(plan.needsValidation)

        return merge(alreadyCheckedFlow, validationFlow).flowOn(dispatcher)
    }
}
