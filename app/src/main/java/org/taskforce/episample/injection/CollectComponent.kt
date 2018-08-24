package org.taskforce.episample.injection

import dagger.Subcomponent
import org.taskforce.episample.auth.LoginViewModel
import org.taskforce.episample.collection.viewmodels.CollectViewModel

@CollectScope
@Subcomponent(modules = [CollectModule::class])
interface CollectComponent {
    fun inject(collectViewModel: CollectViewModel)
    fun inject(loginViewModel: LoginViewModel)
}
