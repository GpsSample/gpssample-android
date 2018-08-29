package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.view.View
import android.widget.Toast
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.Enumeration
import javax.inject.Inject

class NavigationViewModel(application: Application,
                          languageService: LanguageService,
                          val addLandmark: () -> Unit,
                          val back: () -> Unit): AndroidViewModel(application) {

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var collectManager: CollectManager

    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }

    val collectItems = collectManager.getCollectItems()
    val enumerations = Transformations.map( collectManager.getCollectItems(), {
        return@map it.mapNotNull { it as? Enumeration }
    })

    val startRouteText = languageService.getString(R.string.start_route).toUpperCase()
    val landmarkButtonText = languageService.getString(R.string.collect_button_landmark).toUpperCase()

    val navigationTitle = (Transformations.map(enumerations) {
        val enumerations = it
        val enumerationSubject = config.enumerationSubject
        languageService.getString(R.string.navigation_overview_title_var, enumerations.size.toString(), enumerationSubject.plural.capitalize())
    } as MutableLiveData<String>).apply {
        value = languageService.getString(R.string.navigation_overview_title_var, "0", config.enumerationSubject.plural.capitalize())
    }

    fun startRoute(view: View) {
        Toast.makeText(getApplication(), "TODO", Toast.LENGTH_SHORT).show()
    }

    fun addLandmark(view: View) {
        addLandmark()
    }
}