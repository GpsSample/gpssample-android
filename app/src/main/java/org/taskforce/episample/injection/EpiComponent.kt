package org.taskforce.episample.injection

import org.taskforce.episample.auth.LoginAdminDialogFragment
import org.taskforce.episample.auth.LoginFragment
import org.taskforce.episample.collection.ui.CollectAddFragment
import org.taskforce.episample.collection.ui.CollectFragment
import org.taskforce.episample.collection.ui.DuplicateGpsDialogFragment
import org.taskforce.episample.config.base.*
import org.taskforce.episample.config.fields.CustomFieldsAddDropdownDialog
import org.taskforce.episample.config.fields.CustomFieldsAddFragment
import org.taskforce.episample.config.fields.CustomFieldsFragment
import org.taskforce.episample.config.geography.GeographyDialogFragment
import org.taskforce.episample.config.geography.GeographyFragment
import org.taskforce.episample.config.landmark.LandmarkAddFragment
import org.taskforce.episample.config.landmark.LandmarkFragment
import org.taskforce.episample.config.language.LanguageFragment
import org.taskforce.episample.config.language.LanguageImporter
import org.taskforce.episample.config.name.ConfigNameFragment
import org.taskforce.episample.config.sampling.SamplingSelectionFragment
import org.taskforce.episample.config.sampling.SamplingSubsetFragment
import org.taskforce.episample.config.settings.admin.AdminSettingsFragment
import org.taskforce.episample.config.settings.display.DisplaySettingsFragment
import org.taskforce.episample.config.settings.server.ServerSettingsFragment
import org.taskforce.episample.config.settings.user.UserSettingsFragment
import org.taskforce.episample.config.study.StudyCreateFragment
import org.taskforce.episample.config.survey.SurveyExportFragment
import org.taskforce.episample.config.transfer.TransferDownloadDialogFragment
import org.taskforce.episample.main.MainFragment
import org.taskforce.episample.navigation.ui.NavigationFragment
import org.taskforce.episample.navigation.ui.NavigationPlanFragment
import org.taskforce.episample.permissions.ui.PermissionsFragment
import org.taskforce.episample.splash.SplashActivity
import org.taskforce.episample.supervisor.upload.ui.StudyUploadFragment
import org.taskforce.episample.supervisor.upload.ui.StudyUploadViewModel

interface EpiComponent {
    fun plus(collectModule: CollectModule): CollectComponent

    fun inject(splashActivity: SplashActivity)
    fun inject(languageImporter: LanguageImporter)
    fun inject(permissionsFragment: PermissionsFragment)
    fun inject(configFragment: ConfigFragment)
    fun inject(configNameFragment: ConfigNameFragment)
    fun inject(languageFragment: LanguageFragment)
    fun inject(serverSettingsFragment: ServerSettingsFragment)
    fun inject(configStartFragment: ConfigStartFragment)
    fun inject(landmarkAddFragment: LandmarkAddFragment)
    fun inject(landmarkFragment: LandmarkFragment)
    fun inject(surveyExportFragment: SurveyExportFragment)
    fun inject(adminSettingsFragment: AdminSettingsFragment)
    fun inject(customFieldsFragment: CustomFieldsFragment)
    fun inject(samplingSelectionFragment: SamplingSelectionFragment)
    fun inject(userSettingsFragment: UserSettingsFragment)
    fun inject(configSuccessFragment: ConfigSuccessFragment)
    fun inject(customFieldsAddFragment: CustomFieldsAddFragment)
    fun inject(displaySettingsFragment: DisplaySettingsFragment)
    fun inject(loginAdminDialogFragment: LoginAdminDialogFragment)
    fun inject(configAllFragment: ConfigAllFragment)
    fun inject(mainFragment: MainFragment)
    fun inject(studyCreateFragment: StudyCreateFragment)
    fun inject(loginFragment: LoginFragment)
    fun inject(transferDownloadDialogFragment: TransferDownloadDialogFragment)
    fun inject(customFieldsAddDropdownDialog: CustomFieldsAddDropdownDialog)
    fun inject(collectAddFragment: CollectAddFragment)
    fun inject(collectFragment: CollectFragment)
    fun inject(configUploadFragment: ConfigUploadFragment)
    fun inject(geographyDialogFragment: GeographyDialogFragment)
    fun inject(geographyFragment: GeographyFragment)
    fun inject(samplingSubsetFragment: SamplingSubsetFragment)
    fun inject(navigationPlanFragment: NavigationPlanFragment)
    fun inject(navigationFragment: NavigationFragment)
    fun inject(duplicateGpsDialogFragment: DuplicateGpsDialogFragment)
    fun inject(studyUploadViewModel: StudyUploadViewModel)
    fun inject(studyUploadFragment: StudyUploadFragment)
}