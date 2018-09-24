package org.taskforce.episample.injection

import dagger.Subcomponent
import org.taskforce.episample.auth.LoginViewModel
import org.taskforce.episample.collection.viewmodels.CollectAddViewModel
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.core.ui.dialogs.TextInputDialogViewModel
import org.taskforce.episample.main.MainViewModel
import org.taskforce.episample.navigation.ui.*
import org.taskforce.episample.study.ui.SurveyCreateFragment
import org.taskforce.episample.study.ui.SurveyCreateViewModel

@CollectScope
@Subcomponent(modules = [CollectModule::class])
interface CollectComponent {
    fun inject(collectViewModel: CollectViewModel)
    fun inject(loginViewModel: LoginViewModel)
    fun inject(mainViewModel: MainViewModel)
    fun inject(collectAddViewModel: CollectAddViewModel)
    fun inject(navigationPlanViewModel: NavigationPlanViewModel)
    fun inject(navigationToolbarViewModel: NavigationToolbarViewModel)
    fun inject(navigationPlanCardViewModel: NavigationPlanCardViewModel)
    fun inject(liveNavigationCardViewModel: LiveNavigationCardViewModel)
    fun inject(navigationViewModel: NavigationViewModel)
    fun inject(textInputDialogViewModel: TextInputDialogViewModel)
    fun inject(surveyStatusViewModel: SurveyStatusViewModel)
    fun inject(surveyCreateViewModel: SurveyCreateViewModel)
    fun inject(surveyCreateFragment: SurveyCreateFragment)
}
