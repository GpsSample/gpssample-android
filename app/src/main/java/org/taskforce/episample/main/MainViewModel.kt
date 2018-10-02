package org.taskforce.episample.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.text.SpannableStringBuilder
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.UserSession
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.utils.boldSubstring
import java.util.*
import javax.inject.Inject

class MainViewModel(
        application: Application,
        languageService: LanguageService,
        private val lastSynced: Date?,
        collectOnClick: () -> Unit,
        navigateOnClick: () -> Unit,
        syncOnClick: () -> Unit,
        sampleOnClick: () -> Unit,
        finalOnClick: () -> Unit) : AndroidViewModel(application) {


    @Inject
    lateinit var userSession: UserSession

    @Inject
    lateinit var studyRepository: StudyRepository

    val supervisor: Boolean
        get() = userSession.isSupervisor

    init {
        (application as EpiApplication).collectComponent!!.inject(this)
    }

    val studyTitle: LiveData<String> = Transformations.map(studyRepository.getStudy(), {
        // TODO get study from studyRepository
        return@map it!!.name
    })

    init {
        languageService.update =
                {
                    // TODO show last synced date
//            commentary = if (lastSynced != null) {
//                languageService.getString(R.string.main_commentary_last_synced,
//                        config?.displaySettings?.getFormattedDate(lastSynced, false),
//                        config?.displaySettings?.getFormattedTime(lastSynced))
//            } else {
//                languageService.getString(R.string.main_commentary_never_synced)
//            }
                    signIn.value = boldSubstring(languageService.getString(R.string.main_signed_in_as, userSession.username), userSession.username)
                }
    }

    val signIn = MutableLiveData<SpannableStringBuilder>().apply { value = boldSubstring(languageService.getString(R.string.main_signed_in_as, userSession.username), userSession.username) }

    // TODO show last synced date
// languageService.getString(R.string.main_commentary_last_synced,
//    config?.displaySettings?.getFormattedDate(lastSynced, false),
//    config?.displaySettings?.getFormattedTime(lastSynced))
    val commentary = MutableLiveData<String>().apply {
        value = languageService.getString(R.string.main_commentary_never_synced)
    }

    val collectVm = MainItemViewModel(
            R.drawable.collect_graphic,
            languageService.getString(R.string.main_collect_title),
            languageService.getString(R.string.main_collect_description),
            true,
            collectOnClick)

    val navigateVm = MainItemViewModel(
            R.drawable.navigate_graphic,
            languageService.getString(R.string.main_navigate_title),
            languageService.getString(R.string.main_navigate_description),
            true,
            navigateOnClick)

    val syncVm = MainItemViewModel(
            R.drawable.sync_graphic,
            languageService.getString(R.string.main_sync_title),
            languageService.getString(R.string.main_sync_description),
            true,
            syncOnClick)

    val sampleVm = MainItemViewModel(
            R.drawable.sample_graphic,
            languageService.getString(R.string.main_sample_title),
            languageService.getString(R.string.main_sample_description),
            userSession.isSupervisor,
            sampleOnClick)

    val finalVm = MainItemViewModel(
            R.drawable.report_graphic,
            languageService.getString(R.string.main_report_title),
            languageService.getString(R.string.main_report_description),
            userSession.isSupervisor,
            finalOnClick)
}