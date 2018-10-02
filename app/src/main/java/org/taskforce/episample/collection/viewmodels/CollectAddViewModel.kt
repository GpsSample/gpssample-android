package org.taskforce.episample.collection.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import android.net.Uri
import android.os.CountDownTimer
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Single
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.R
import org.taskforce.episample.collection.ui.CollectGpsPrecisionViewModel
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.LiveDataTriple
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.db.config.customfield.value.*
import java.util.*
import javax.inject.Inject

class CollectAddViewModel(
        application: Application,
        languageService: LanguageService,
        mapObservable: Single<GoogleMap>,
        val isLandmark: Boolean,
        private val saveButtonEnabledColor: Int,
        private val saveButtonDisabledColor: Int,
        private val saveButtonEnabledTextColor: Int,
        private val saveButtonDisabledTextColor: Int,
        private val goToNext: () -> Unit,
        val takePicture: () -> Unit,
        private val showDuplicateGpsDialog: (enumeration: Enumeration?, subject: EnumerationSubject) -> Unit,
        private val showOutsideEnumerationAreaDialog: (latLng: LatLng, precision: Double) -> Unit) : AndroidViewModel(application) {

    @Inject
    lateinit var userSession: UserSession

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var collectManager: CollectManager

    @Inject
    lateinit var locationService: LocationService

    init {
        (application as EpiApplication).collectComponent?.inject(this)

        locationService.configuration = LocationServiceConfiguration(minTime = 5000, minDistance = 0f)
    }

    var landmarkType = config.landmarkTypes.first()

    var gpsVm: CollectGpsPrecisionViewModel? = null

    val gpsBreadcrumbs = collectManager.getBreadcrumbs()

    val collectItems = collectManager.getCollectItems()

    val landmarkTypes = collectManager.getLandmarkTypes()

    val customFields = config.customFields

    init {
        languageService.update = {
            showPhotoText.postValue(languageService.getString(R.string.collect_add_text))
            excludeText.postValue(languageService.getString(R.string.collect_exclude))
            notesHint.set(languageService.getString(R.string.collect_notes_hint))
            photoText.set(languageService.getString(R.string.collect_add_text))
            gpsDisplay.set(languageService.getString(R.string.collect_gps_waiting))
        }
    }

    val showPhotoButton = config.userSettings.allowPhotos

    val showPhotoCard = config.userSettings.allowPhotos

    val showPhotoText = MutableLiveData<String>().apply {
        value = languageService.getString(R.string.collect_add_text)
    }
    
    val showTakePhoto = MutableLiveData<Boolean>().apply { value = true }

    val exclude = MutableLiveData<Boolean>().apply { value = false }

    var showExclude = MutableLiveData<Boolean>().apply { value = !isLandmark }

    val excludeText = MutableLiveData<String>().apply {
        value = languageService.getString(R.string.collect_exclude)
    }

    val notesError = MutableLiveData<String>().apply { value = "" }

    val notesHint = ObservableField(languageService.getString(R.string.collect_notes_hint))

    val notes = MutableLiveData<String>()

    val primaryLabelError = MutableLiveData<String>().apply { value = "" }

    val primaryLabelHint = "${config.enumerationSubject.primaryLabel} *"

    val primaryLabelErrorEnabled = MutableLiveData<Boolean>().apply { value = false }

    val primaryLabel = MutableLiveData<String>().apply { value = "" }

    val enumerations = collectManager.getEnumerations()

    var location = MutableLiveData<LatLng?>().apply { value = null }

    var locationLatLng: LatLng? = null
    var locationPrecision: Float? = null

    val notesErrorEnabled = MutableLiveData<Boolean>().apply { value = false }

    val selectedLandmark = MutableLiveData<LandmarkType>()

    val landmarkImage = Transformations.map(selectedLandmark) { it.iconLocation }

    var landmarkName = MutableLiveData<String>().apply { value = "" }
    
    var image: String? = null

    private val customFieldMediatorLiveData = MediatorLiveData<Boolean>().apply { value = false }

    private val enumerationInputs = LiveDataTriple(primaryLabel, location, customFieldMediatorLiveData)
    private val isEnumerationValid = Transformations.map(enumerationInputs) {
        val primaryLabel = it.first

        determineEnumerationValidity(primaryLabel)
    } as MutableLiveData

    private fun determineEnumerationValidity(primaryLabel: String?): Boolean {
        val primaryLabelComplete = !primaryLabel.isNullOrBlank()

        val customFieldsComplete = customFieldViewModels.filter { customVM ->
            customVM.customField.isRequired || customVM.customField.isAutomatic
        }.fold(true) { acc, next ->
            when (next) {
                is CustomTextViewModel -> acc && next.value.value != null && !next.value.value.isNullOrBlank()
                is CustomDropdownViewModel -> acc && next.value.value != null
                is CustomNumberViewModel -> acc && next.value.value != null && !next.value.value.isNullOrBlank()
                is CustomDateViewModel -> acc && next.value.value != null
                is CustomCheckboxViewModel -> acc && next.value.value != null
                else -> acc && next.value != null
            }
        }
        return !isLandmark &&
                primaryLabelComplete &&
                (isSufficientlyPrecise || useDuplicatedGps) &&
                customFieldsComplete
    }

    private val landmarkInputs = LiveDataTriple(landmarkName, selectedLandmark, location)

    private val isLandmarkValid = Transformations.map(landmarkInputs) {
        val landmarkName = it.first

        isLandmark &&
                !landmarkName.isNullOrEmpty() &&
                selectedLandmark.value != null
    }
    val validPair = LiveDataPair(isEnumerationValid, isLandmarkValid)

    val isValidLive = Transformations.map(validPair) {
        val validEnumeration = it.first ?: false
        val validLandmark = it.second ?: false

        validEnumeration || validLandmark
    }
    val poorGpsError = MutableLiveData<String>().apply { value = languageService.getString(R.string.collect_poor_gps) }

    val showPoorGps = Transformations.map(isValidLive) {
        if (isLandmark) {
            false
        } else {
            !isSufficientlyPrecise && duplicateGpsEnabled.value ?: true
        }
    }

    val collectGpsText = MutableLiveData<String>().apply { value = languageService.getString(R.string.collect_duplicate_gps) }

    val duplicateGpsEnabled: MutableLiveData<Boolean> = (Transformations.map(enumerations) {
        it.isNotEmpty()
    } as MutableLiveData<Boolean>).apply { value = false }

    val showDuplicateIcon = MutableLiveData<Boolean>().apply { value = false }

    var useDuplicatedGps = false
    var mostRecentEnumeration: Enumeration? = null

    fun showDuplicateGpsDialog() {
        showDuplicateGpsDialog(mostRecentEnumeration, config.enumerationSubject)
    }

    fun duplicateGps(lastEnumeration: Enumeration?) {
        lastEnumeration?.let {
            showDuplicateIcon.postValue(true)

            cancelCountdown()
            useDuplicatedGps = true
            gpsVm?.precision?.set(it.gpsPrecision)
            locationLatLng = lastEnumeration.location
            locationPrecision = it.gpsPrecision.toFloat()
            gpsDisplay.set("%.5f ".format(lastEnumeration.location.latitude) +
                    ", %.5f".format(lastEnumeration.location.longitude))
            customFieldMediatorLiveData.postValue(false) // Trigger update of isEnumerationValid
        }

        duplicateGpsEnabled.postValue(false)
    }

    var countdownText = languageService.getString(R.string.collect_save_incomplete)

    val saveEnumerationButtonText: MutableLiveData<String> = (Transformations.map(isEnumerationValid) {
        val isEnumerationValid = it
        val subject = config.enumerationSubject

        val isCountdownNew = !countdown.isRunning && !countdownDone // Not running and not done means it hasn't been started yet

        if (isEnumerationValid) { // Completely valid. GPS is within preferred precision and all required fields filled
            cancelCountdown()
            languageService.getString(R.string.collect_save_complete, subject.singular.toUpperCase())
        } else if (!isSufficientlyPrecise && !useDuplicatedGps) { // GPS not precise enough and haven't decided to use the previous value
            if (isCountdownNew) { // Countdown hasn't started yet
                startCountdown()
                countdownText
            } else if (countdown.isRunning) {
                countdownText
            } else { // Countdown finished
                languageService.getString(R.string.collect_save_incomplete)
            }
        } else { // GPS is precise enough (or using duplicate) but required fields aren't filled in
            languageService.getString(R.string.collect_save_incomplete)
        }
    } as MutableLiveData<String>).apply { value = languageService.getString(R.string.collect_save_incomplete) }

    val saveLandmarkButtonText = MutableLiveData<String>().apply { value = languageService.getString(R.string.collect_save_landmark) }

    var countdownDone = false

    private fun startCountdown() {
        countdown.isRunning = true
        countdown.start()
    }

    private fun cancelCountdown() {
        countdown.cancel()
        countdownDone = true
    }

    private val countdown = object : CountDownTimer(countdownLength, countdownTick) {
        var isRunning = false

        override fun onFinish() {
            isRunning = false
            if (!isSufficientlyPrecise) {
                countdownDone = true
                customFieldMediatorLiveData.postValue(customFieldMediatorLiveData.value)
                countdownText = languageService.getString(R.string.collect_save_incomplete)
            }
        }

        override fun onTick(timeRemaining: Long) {
            isRunning = true
            if (!isSufficientlyPrecise) {
                val sec = (timeRemaining / 1000) % 60
                val min = ((timeRemaining / 1000) / 60) % 60
                val timeRemainingFormatted = String.format("%2d:%02d", min, sec)
                val timeRemainingText = languageService.getString(R.string.collect_save_countdown, timeRemainingFormatted)
                countdownText = timeRemainingText
                saveEnumerationButtonText.postValue(countdownText)
            }
        }
    }

    private val canSaveItem: Boolean // True even if it's incomplete
        get() = (isSufficientlyPrecise || countdownDone) || // Either the GPS is precise enough or the countdown is over
                (isLandmark && (isLandmarkValid.value == true && !landmarkName.value.isNullOrBlank())) // If landmark, check the name

    val saveEnumerationButtonTextColor = (Transformations.map(isEnumerationValid) {
        if (canSaveItem) {
            saveButtonEnabledTextColor
        } else {
            saveButtonDisabledTextColor
        }
    } as MutableLiveData).apply { value = saveButtonDisabledTextColor }

    val saveLandmarkButtonTextColor = (Transformations.map(isLandmarkValid) { landmarkValid ->
        if (landmarkValid) {
            saveButtonEnabledTextColor
        } else {
            saveButtonDisabledTextColor
        }
    } as MutableLiveData).apply { value = saveButtonDisabledTextColor }

    val saveEnumerationButtonBackground = (Transformations.map(isEnumerationValid) {
        if (canSaveItem) {
            saveButtonEnabledColor
        } else {
            saveButtonDisabledColor
        }
    } as MutableLiveData).apply { value = saveButtonDisabledColor }

    val saveLandmarkButtonBackground = (Transformations.map(isLandmarkValid) {
        if (it) {
            saveButtonEnabledColor
        } else {
            saveButtonDisabledColor
        }
    } as MutableLiveData).apply { value = saveButtonDisabledColor }

    val photoText = ObservableField(languageService.getString(R.string.collect_add_text))

    val landmarkHint = ObservableField(languageService.getString(R.string.collect_landmark_name_hint))

    val landmarkError = ObservableField("")

    val landmarkErrorEnabled = ObservableField(false)

    val gpsDisplay = ObservableField(languageService.getString(R.string.collect_gps_waiting))

    private val customFieldViewModels = mutableListOf<AbstractCustomViewModel>()

    private val isSufficientlyPrecise
        get() = (locationPrecision ?: 9999.0f <= config.userSettings.gpsPreferredPrecision)

    var googleMap: GoogleMap? = null
    var lastKnownLocation: LatLng? = null

    init {
        useDuplicatedGps = false // reset

        mapObservable.subscribe { map ->
            googleMap = map
            @SuppressLint("MissingPermission")
            map.isMyLocationEnabled = true
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            locationLatLng?.let { latLng ->
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mapZoom))
            }
        }
    }

    fun addCustomFieldViewModel(viewModel: AbstractCustomViewModel) {
        customFieldViewModels.add(viewModel)
        customFieldMediatorLiveData.addSource((viewModel.value) as MutableLiveData<*>) {
            // Triggers the MediatorLiveData with a new value so isEnumerationValid can recalculate
            customFieldMediatorLiveData.postValue(!(customFieldMediatorLiveData.value ?: false))
        }
    }

    fun saveItem() {
        if (canSaveItem) {
            locationLatLng?.let { latLng ->
                gpsVm?.precision?.get()?.let { gpsPrecision ->
                    if (isLandmark) {
                        saveLandmark(latLng, gpsPrecision)
                    } else {
                        if (!isWithinEnumerationAreas()) {
                            showOutsideEnumerationAreaDialog(latLng, gpsPrecision)
                        } else {
                            saveEnumeration(latLng, gpsPrecision)
                        }
                    }
                }
            }
        }
    }
    
    private fun isWithinEnumerationAreas(): Boolean {
        return if (config.enumerationAreas.isNotEmpty()) {
            config.enumerationAreas.any { area ->
                locationLatLng?.let { location ->
                    area.isWithinArea(location)
                } ?: false
            }
        } else {
            true
        }
    }

    private fun saveLandmark(latLng: LatLng, gpsPrecision: Double) {
        collectManager.addLandmark(
                LiveLandmark(
                        userSession.username,
                        landmarkName.value ?: "",
                        landmarkType,
                        notes.value,
                        image,
                        latLng,
                        gpsPrecision,
                        id = null
                )) {
            goToNext()
        }
    }

    fun saveEnumeration(latLng: LatLng, gpsPrecision: Double, shouldExclude: Boolean = false) {
        val customFields = customFieldViewModels.mapNotNull { customVm ->
            val type = customVm.customField.type

            val customFieldValue = when (customVm) {
                is CustomNumberViewModel -> {
                    customVm.value.value?.let {
                        when {
                            customVm.isInteger -> IntValue(it.toInt())
                            else -> DoubleValue(it.toDouble())
                        }
                    }
                }
                is CustomTextViewModel -> customVm.value.value?.let { TextValue(it) }
                is CustomCheckboxViewModel -> customVm.value.value?.let { BooleanValue(it) }
                is CustomDropdownViewModel -> customVm.value.value?.key?.let { DropdownValue(it) }
                is CustomDateViewModel -> customVm.value.value?.let { DateValue(it) }
                else -> null
            }

            customFieldValue?.let {
                LiveCustomFieldValue(customFieldValue, type, customVm.customField.id)
            }
        }
        val isIncomplete = !(isEnumerationValid.value ?: false)
        val isExcluded = (exclude.value ?: false) || shouldExclude
        collectManager.addEnumerationItem(LiveEnumeration(
                userSession.username,
                image,
                isIncomplete,
                isExcluded,
                primaryLabel.value ?: "",
                notes.value,
                latLng,
                gpsPrecision,
                "Display Date",
                customFields,
                id = null)) {
            goToNext()
        }
    }
    
    fun takePhoto(view: View) {
        takePicture()
    }
    
    fun setImage(photoUri: Uri) {
        image = photoUri.toString()
        
        showTakePhoto.postValue(false)
    }

    fun updateDateField(customFieldId: String, date: Date) {
        customFieldViewModels.firstOrNull {
            it.customField.id == customFieldId
        }?.let {
            (it as CustomDateViewModel).value.postValue(date)
        }
    }

    companion object {
        val mapZoom = 18.0f
        const val countdownLength: Long = 120000 // 2 minutes in milliseconds
        const val countdownTick: Long = 1000 // 1 second in milliseconds
    }
}
