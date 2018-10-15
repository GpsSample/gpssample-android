package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.*
import android.location.Location
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.ui.CollectGpsPrecisionViewModel
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.core.language.LanguageService
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.utils.getResourceUri
import javax.inject.Inject

class NavigationPlanCardViewModel(application: Application,
                                  userSettings: UserSettings?,
                                  lastKnownLocation: LiveData<Pair<LatLng, Float>>,
                                  lowestColor: Int,
                                  mediumColor: Int,
                                  highestColor: Int,
                                  private val viewPhoto: (String?) -> Unit) : AndroidViewModel(application), NavigationCardViewModel {

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var languageService: LanguageService

    init {
        (application as EpiApplication).collectComponent?.inject(this)
    }

    override val itemData: MutableLiveData<CollectItem> = MutableLiveData<CollectItem>()
    private val distanceInputPair = LiveDataPair(itemData, lastKnownLocation)

    private val itemDataObserver: Observer<CollectItem> = Observer {
        gpsPrecisionVm.precision.set(it?.gpsPrecision)
    }

    init {
        itemData.observeForever(itemDataObserver)
    }

    override fun onCleared() {
        super.onCleared()
        itemData.removeObserver(itemDataObserver)
    }

    override val buttonLayoutVisibility = MutableLiveData<Boolean>().apply { value = false }
    override val primaryButtonText = MutableLiveData<String?>()
    override val secondaryButtonText = MutableLiveData<String?>()

    override val navigationStatus: LiveData<String> = Transformations.switchMap(itemData, {
        val statusString = when (it) {
            is Landmark -> languageService.getString(R.string.navigation_card_landmark_title, it.title)
            is NavigationItem -> when (it.surveyStatus) {
                is SurveyStatus.Incomplete -> languageService.getString(R.string.survey_status_awaiting)
                is SurveyStatus.Complete -> languageService.getString(R.string.survey_status_complete)
                is SurveyStatus.Problem -> languageService.getString(R.string.survey_status_problem)
                is SurveyStatus.Skipped -> languageService.getString(R.string.survey_status_skipped)
            }
            else -> throw IllegalStateException()
        }
        return@switchMap Transformations.map(statusString, {
            return@map it.toUpperCase()
        })
    })

    override val imageUrl: LiveData<String?> = Transformations.map(itemData) {
        return@map if (it.image.isNullOrBlank()) {
            application.resources.getResourceUri(R.drawable.icon_image_placeholder_darkgray_24).toString()
        } else {
            it.image
        }
    }

    override fun viewPhotoAction(view: View) {
        viewPhoto(imageUrl.value)
    }

    override val title: LiveData<String?> = Transformations.map(itemData, {
        return@map it.title
    })

    override val distance: LiveData<String> = Transformations.map(distanceInputPair) {
        val item = it.first
        val latLng = it.second.first

        val results = FloatArray(3)
        val itemLocation = item.location
        Location.distanceBetween(latLng.latitude,
                latLng.longitude,
                itemLocation.latitude,
                itemLocation.longitude,
                results)

        return@map String.format("%.1f km away", results[0] / 1000)
    }

    override val showDetailsText = Transformations.map(itemData){
        !it.note.isNullOrBlank()
    }

    override val detailsText: LiveData<SpannableString> = Transformations.map(itemData, {
        val detailsFormat = "Notes: %s"
        val details = detailsFormat.format(it.note ?: "Example note")
        val spannableString = SpannableString(details)
        spannableString.setSpan(StyleSpan(android.graphics.Typeface.BOLD),
                0,
                detailsFormat.length - 3,
                0
        )
        return@map spannableString
    })

    override val visibility = MutableLiveData<Boolean>().apply { value = false }

    override val gpsPrecisionVm = CollectGpsPrecisionViewModel(userSettings?.gpsMinimumPrecision ?: 0.0,
            userSettings?.gpsPreferredPrecision ?: 0.0,
            lowestColor,
            mediumColor,
            highestColor)
}