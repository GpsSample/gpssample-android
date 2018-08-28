package org.taskforce.episample.collection.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.Single
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.ui.CollectGpsPrecisionViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.LiveDataTriple
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.fileImport.models.LandmarkType
import javax.inject.Inject

class CollectAddViewModel(
        application: Application,
        languageService: LanguageService,
        landmarkObservable: Observable<LandmarkType>,
        mapObservable: Single<GoogleMap>,
        locationObservable: Observable<Location>,
        val isLandmark: Boolean,
        private val saveButtonEnabledColor: Int,
        private val saveButtonDisabledColor: Int,
        private val saveButtonEnabledTextColor: Int,
        private val saveButtonDisabledTextColor: Int,
        private val goToNext: () -> Unit,
        private val takePicture: () -> Unit) : AndroidViewModel(application) {

    init {
        (application as EpiApplication).collectComponent?.inject(this)
    } 

    @Inject
    lateinit var collectManager: CollectManager

    var gpsVm: CollectGpsPrecisionViewModel? = null

    val gpsBreadcrumbs = collectManager.getBreadcrumbs()

    val collectItems = collectManager.getCollectItems()

    val userSettings = collectManager.getUserSettings()

    val enumerationSubject = collectManager.getEnumerationSubject()

    val landmarkTypes = collectManager.getLandmarkTypes()

    init {
        languageService.update = {
            showPhotoText.postValue(languageService.getString(R.string.collect_add_text))
            excludeText.postValue(languageService.getString(R.string.collect_exclude))
            notesHint.set(languageService.getString(R.string.collect_notes_hint))
            photoText.set(languageService.getString(R.string.collect_add_text))
            gpsDisplay.set(languageService.getString(R.string.collect_gps_waiting))
        }
        landmarkObservable.subscribe {
            selectedLandmark.postValue(it)
        }
        locationObservable.subscribe {
            gpsDisplay.set("${it.latitude}, ${it.longitude}")
        }
    }

    val showPhotoButton = Transformations.map(userSettings) {
        it.allowPhotos
    }

    val showPhotoCard = Transformations.map(userSettings) { it.allowPhotos }

    val showPhotoText = MutableLiveData<String>().apply {
        value = languageService.getString(R.string.collect_add_text)
    }

    val exclude = MutableLiveData<Boolean>().apply { value = false }

    var showExclude = MutableLiveData<Boolean>().apply { value = !isLandmark }

    val excludeText = MutableLiveData<String>().apply {
        value = languageService.getString(R.string.collect_exclude)
    }

    val notesError = MutableLiveData<String>().apply { value = "" }

    val notesHint = ObservableField(languageService.getString(R.string.collect_notes_hint))

    val notes = MutableLiveData<String>()
    
    val showCustomFields = MutableLiveData<Boolean>().apply { value = !isLandmark }
    
    val primaryLabelError = MutableLiveData<String>().apply { value = "" }
    
    val primaryLabelHint = Transformations.map(enumerationSubject) { "${it.primaryLabel} *" }
    
    val primaryLabelErrorEnabled = MutableLiveData<Boolean>().apply { value = false }
    
    val primaryLabel = MutableLiveData<String>().apply { value = "" }

    var location = MutableLiveData<Location?>().apply { value = null }

    val notesErrorEnabled = MutableLiveData<Boolean>().apply { value = false }

    val selectedLandmark = MutableLiveData<LandmarkType>()

    val landmarkImage = Transformations.map(selectedLandmark) { it.iconLocation }

    var landmarkName = MutableLiveData<String>().apply { value = "" }

    // TODO: add the custom fields as a source
    val enumerationInputs = LiveDataPair(primaryLabel, location)
    val isEnumerationValid = Transformations.map(enumerationInputs) {
        val primaryLabel = it.first

        !isLandmark && 
                !primaryLabel.isNullOrBlank() && 
                isSufficientlyPrecise
    }

    val landmarkInputs = LiveDataTriple(landmarkName, selectedLandmark, location)
    val isLandmarkValid = Transformations.map(landmarkInputs) {
        val landmarkName = it.first

        isLandmark &&
                isSufficientlyPrecise &&
                !landmarkName.isNullOrEmpty() &&
                selectedLandmark.value != null &&
                customFieldViewModels.filter {
                    it.customField.isRequired || it.customField.isAutomatic
                }.fold(true) { acc, next ->
                    acc && next.value != null
                }
    }


    val validPair = LiveDataPair(isEnumerationValid, isLandmarkValid)
    val isValidLive = Transformations.map(validPair) {
        val validEnumeration = it.first ?: false
        val validLandmark = it.second ?: false

        validEnumeration || validLandmark
    }

    // TODO: If not sufficiently precise, start a countdown that disables the button
    val saveButtonPair = LiveDataPair(isEnumerationValid, enumerationSubject)
    val saveButtonText = Transformations.map(saveButtonPair) {
        val isEnumerationValid = it.first ?: false
        val subject = it.second
        
        if (isLandmark) {
            languageService.getString(R.string.collect_save_landmark)
        } else {
            if (isEnumerationValid) {
                languageService.getString(R.string.collect_save_complete, subject?.singular?.toUpperCase() ?: "")
            } else {
                languageService.getString(R.string.collect_save_incomplete)
            }
        }
    }

    val saveButtonTextColor: LiveData<Int> = Transformations.map(isValidLive) {
        if (isSufficientlyPrecise) {
            saveButtonEnabledTextColor
        } else {
            saveButtonDisabledTextColor
        }
    }

    val saveButtonBackground: LiveData<Int> = Transformations.map(isValidLive) {
        if (isSufficientlyPrecise) {
            saveButtonEnabledColor
        } else {
            saveButtonDisabledColor
        }
    }

    val photoText = ObservableField(languageService.getString(R.string.collect_add_text))

    val landmarkHint = ObservableField(languageService.getString(R.string.collect_landmark_name_hint))

    val landmarkError = ObservableField("")

    val landmarkErrorEnabled = ObservableField(false)

    val gpsDisplay = ObservableField(languageService.getString(R.string.collect_gps_waiting))

    private val customFieldViewModels = mutableListOf<AbstractCustomViewModel>()

    private val isSufficientlyPrecise
        get() = (location.value?.accuracy ?: 9999.0f < userSettings.value?.gpsPreferredPrecision ?: 0.0)

//    private val isValid
//        get() =
//            if (isLandmark) {
//                isSufficientlyPrecise && (landmarkName.value?.isNotBlank() ?: false)
//            } else {
//                isSufficientlyPrecise &&
//                        customFieldViewModels.filter {
//                            it.customField.isRequired || it.customField.isAutomatic
//                        }.fold(true, { acc, next ->
//                            acc && next.value != null
//                        })
//            }

    var googleMap: GoogleMap? = null

    init {
        mapObservable.subscribe { map ->
            googleMap = map
            @SuppressLint("MissingPermission")
            map.isMyLocationEnabled = true
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            location.value?.let { location ->
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18.0f))
            }
        }
        
        locationObservable.subscribe {
            if (it.accuracy < (location.value?.accuracy ?: 1000.0f)) {
                if (location.value == null) {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
                }
                location.postValue(it)
                gpsDisplay.set("%.5f ".format(it.latitude) + ", %.5f".format(it.longitude))
                gpsVm?.precision?.set(it.accuracy.toDouble())
            }
        }
    }

    fun addCustomFieldViewModel(viewModel: AbstractCustomViewModel) {
        customFieldViewModels.add(viewModel)
    }

    fun saveItem() {
        val isValid = isValidLive.value ?: false
        if (isValid) {
            // TODO insert record into db
//            if (isLandmark) {
//                studyManager.addLandmark(landmarkItem)
//            } else {
//                studyManager.addEnumerationPoint(enumerationItem)
//            }
            goToNext()
        }
    }
}
