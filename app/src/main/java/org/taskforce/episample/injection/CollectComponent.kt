package org.taskforce.episample.injection

import dagger.Subcomponent
import org.taskforce.episample.auth.LoginViewModel
import org.taskforce.episample.collection.ui.CollectAddFragment
import org.taskforce.episample.collection.ui.CollectDetailsFragment
import org.taskforce.episample.collection.ui.CollectDetailsViewModel
import org.taskforce.episample.collection.viewmodels.CollectAddViewModel
import org.taskforce.episample.collection.viewmodels.CollectViewModel
import org.taskforce.episample.core.ui.dialogs.TextInputDialogViewModel
import org.taskforce.episample.main.MainViewModel
import org.taskforce.episample.navigation.ui.*
import org.taskforce.episample.sampling.ui.GenerateSampleFragment
import org.taskforce.episample.sampling.ui.SamplesFragment
import org.taskforce.episample.sampling.ui.SamplingFragment
import org.taskforce.episample.sampling.ui.SamplingGenerationViewModel

@CollectScope
@Subcomponent(modules = [CollectModule::class])
interface CollectComponent {
    fun inject(collectViewModel: CollectViewModel)
    fun inject(loginViewModel: LoginViewModel)
    fun inject(mainViewModel: MainViewModel)
    fun inject(collectAddViewModel: CollectAddViewModel)
    fun inject(collectAddFragment: CollectAddFragment)
    fun inject(navigationPlanViewModel: NavigationPlanViewModel)
    fun inject(navigationToolbarViewModel: NavigationToolbarViewModel)
    fun inject(navigationPlanCardViewModel: NavigationPlanCardViewModel)
    fun inject(liveNavigationCardViewModel: LiveNavigationCardViewModel)
    fun inject(navigationViewModel: NavigationViewModel)
    fun inject(textInputDialogViewModel: TextInputDialogViewModel)
    fun inject(surveyStatusViewModel: SurveyStatusViewModel)
    fun inject(samplingGenerationViewModel: SamplingGenerationViewModel)
    fun inject(generateSampleFragment: GenerateSampleFragment)
    fun inject(samplingFragment: SamplingFragment)
    fun inject(samplesFragment: SamplesFragment)
    fun inject(navigationListViewModel: NavigationListViewModel)
    fun inject(collectDetailsViewModel: CollectDetailsViewModel)
    fun inject(collectDetailsFragment: CollectDetailsFragment)
    fun inject(navigationDetailsViewModel: NavigationDetailsViewModel)
    fun inject(navigationDetailsFragment: NavigationDetailsFragment)
}
