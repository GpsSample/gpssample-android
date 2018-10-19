package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.location.Location
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.viewmodels.CollectDetailField
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.core.language.LanguageService
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.core.util.DistanceUtil
import org.taskforce.episample.db.config.customfield.CustomFieldType
import javax.inject.Inject

class NavigationDetailsViewModel(application: Application,
                                 navigationItem: NavigationItem,
                                 private val incompleteColor: Int,
                                 private val completeColor: Int,
                                 private val skippedColor: Int,
                                 private val problemColor: Int,
                                 private val changeStatus: (NavigationItem) -> Unit) : AndroidViewModel(application) {

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var locationService: LocationService
    
    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var languageService: LanguageService
    
    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }
    
    val data = navigationManager.getNavigationItem(navigationItem.navigationPlanId, navigationItem.id!!)

    val showPhotoCard = Transformations.map(data) {
        !it.image.isNullOrBlank()
    }
    
    val statusText = Transformations.switchMap(LiveDataPair(data, languageService.currentLanguage)) { (navItem, _) ->
        val surveyStatus = navItem.surveyStatus
        when (surveyStatus) {
            is SurveyStatus.Incomplete -> {
                languageService.getString(R.string.nav_status_incomplete)
            }
            is SurveyStatus.Complete -> {
                languageService.getString(R.string.nav_status_complete)
            }
            is SurveyStatus.Problem -> {
                languageService.getString(R.string.nav_status_problem)
            }
            is SurveyStatus.Skipped -> {
                languageService.getString(R.string.nav_status_skipped)
            }
        }
    }
    
    val statusColor = (Transformations.map(data) {
        val surveyStatus = it.surveyStatus
        when (surveyStatus) {
            is SurveyStatus.Incomplete -> {
                incompleteColor
            }
            is SurveyStatus.Complete -> {
                completeColor
            }
            is SurveyStatus.Problem -> {
                problemColor
            }
            is SurveyStatus.Skipped -> {
                skippedColor
            }
        }
    } as MutableLiveData<Int>).apply { 
        value = incompleteColor // Default
    }
    
    val showSurveyDate = Transformations.map(data) {
        it.surveyStatus != SurveyStatus.Incomplete()
    }
    
//    val surveyDate = Transformations.map(data) {
//        it.
//    }

    val showIncomplete = Transformations.map(data) { collectItem ->
        when (collectItem) {
            is Enumeration -> collectItem.isIncomplete
            else -> false
        }
    }

    val photoSource = Transformations.map(data) {
        it.image
    }

    private val locationPair = LiveDataPair(data, locationService.locationLiveData)
    val distanceText: LiveData<String> = Transformations.map(locationPair) {
        val currentLocation = it.second.first
        val distanceArray = FloatArray(3)
        val itemLocation = it.first.location

        Location.distanceBetween(currentLocation.latitude,
                currentLocation.longitude,
                itemLocation.latitude,
                itemLocation.longitude,
                distanceArray)

        DistanceUtil.convertMetersToString(distanceArray[0])
    }

    val gpsDisplay = Transformations.map(data) {
        val latLng = it.location
        "%.5f ".format(latLng.latitude) + ", %.5f".format(latLng.longitude)
    }

    val customFields: LiveData<List<CollectDetailField>> = Transformations.map(data) { navigationItem ->
        val fields: MutableList<CollectDetailField> = mutableListOf()
        
        val titleField = CollectDetailField(navigationItem, "${config.enumerationSubject.primaryLabel}:", navigationItem.title)
        fields.add(titleField)
        
        config.customFields.forEach { customField ->
            val customFieldValue = navigationItem.customFieldValues.firstOrNull {
                it.customFieldId == customField.id
            }

            var isCheckbox = when (customField.type) {
                CustomFieldType.CHECKBOX -> true
                else -> false
            }

            if (!customField.isAutomatic) {
                var value = customFieldValue?.getValueForCustomField(customField, config.displaySettings) ?: ""
                val isIncomplete = customField.isRequired && value.isBlank()

                if (value.isBlank()) {
                    value = "Empty"
                }

                if (isCheckbox && (value == "Empty" || value == "false")) {
                    value = "Unchecked"
                    isCheckbox = false
                }

                val field = CollectDetailField(navigationItem, "${customField.name}:", value, isCheckbox, isIncomplete)
                fields.add(field)
            }
        }

        navigationItem.note?.let { note ->
            fields.add(CollectDetailField(navigationItem, "Notes:", note)) // TODO: Translate the word "Note"
        }

        return@map fields
    }
    
    fun changeStatus() {
        data.value?.let {
            changeStatus(it)
        }
    }
}