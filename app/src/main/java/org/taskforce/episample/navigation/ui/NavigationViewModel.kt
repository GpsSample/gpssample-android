package org.taskforce.episample.navigation.ui

import android.app.Application

import android.arch.lifecycle.*
import android.view.View
import android.widget.Toast
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.core.interfaces.LocationService
import org.taskforce.episample.core.language.LanguageService
import javax.inject.Inject
class NavigationViewModel(application: Application,
                          val addLandmark: () -> Unit): AndroidViewModel(application) {

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var collectManager: CollectManager

    @Inject
    lateinit var languageService: LanguageService

    @Inject
    lateinit var locationService: LocationService

    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }

    override fun onCleared() {
        super.onCleared()
        languageService.cleanup()
    }
    val collectItems = collectManager.getCollectItems()

    val enumerations: LiveData<List<Enumeration>> = Transformations.map( collectManager.getCollectItems()) {
        return@map it.mapNotNull { it as? Enumeration }
    }

    val startRouteText: LiveData<String> = Transformations.map(languageService.getString(R.string.start_route)) {
        return@map it.toUpperCase()
    }

    val landmarkButtonText: LiveData<String> = Transformations.map(languageService.getString(R.string.collect_button_landmark)) {
        return@map it.toUpperCase()
    }

    private val enumerationsToSurvey = (Transformations.map(enumerations) {
        return@map it.size
    } as MutableLiveData<Int>).apply { value = 0 }
    val navigationTitle: LiveData<LiveData<String>> = Transformations.map(enumerationsToSurvey) {
        val numberToSurvey = it
        val enumerationSubject = config.enumerationSubject
        languageService.getString(R.string.navigation_overview_title_var, numberToSurvey.toString(), enumerationSubject.plural.capitalize())
    }

    fun startRoute(view: View) {
        Toast.makeText(getApplication(), "TODO", Toast.LENGTH_SHORT).show()
    }

    fun addLandmark(view: View) {
        addLandmark()
    }
}