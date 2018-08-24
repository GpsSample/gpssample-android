package org.taskforce.episample.collection.viewmodels

import android.annotation.SuppressLint
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.location.Location
import android.widget.ArrayAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import org.taskforce.episample.BR
import org.taskforce.episample.R
import org.taskforce.episample.collection.models.EnumerationItem
import org.taskforce.episample.collection.models.LandmarkItem
import org.taskforce.episample.collection.ui.CollectGpsPrecisionViewModel
import org.taskforce.episample.config.fields.CustomFieldDataItem
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.fileImport.models.LandmarkType
import org.taskforce.episample.utils.bindDelegate

class CollectAddViewModel(
        languageService: LanguageService,
        landmarkObservable: Observable<LandmarkType>,
        mapObservable: Observable<GoogleMap>,
        locationObservable: Observable<Location>,
        val isLandmark: Boolean,
        private val saveButtonEnabledColor: Int,
        private val saveButtonDisabledColor: Int,
        private val saveButtonEnabledTextColor: Int,
        private val saveButtonDisabledTextColor: Int,
        val landmarkOptionAdapter: ArrayAdapter<String>,
        minimumPrecision: Double,
        preferredPrecision: Double,
        lowestColor: Int,
        mediumColor: Int,
        highestColor: Int,
        private val goToNext: () -> Unit) : BaseObservable() {

    // TODO set once using db
    private val gpsMinimumPrecision: Double = 20.0
    private val enumerationSubject: String = "Households"

    init {
        languageService.update = {
            showPhotoText = languageService.getString(R.string.collect_add_text)
            excludeText = languageService.getString(R.string.collect_exclude)
            notesHint = languageService.getString(R.string.collect_notes_hint)
            saveButtonText = languageService.getString(R.string.collect_save_incomplete)
            photoText = languageService.getString(R.string.collect_add_text)
            gpsDisplay = languageService.getString(R.string.collect_gps_waiting)
        }
        landmarkObservable.subscribe {
            selectedLandmark = it
        }
        locationObservable.subscribe {
            gpsDisplay = "${it.latitude},${it.longitude}"
        }
    }

    @get:Bindable
    var showPhoto by bindDelegate(false, { _, _ ->
        notifyPropertyChanged(BR.showPhotoButton)
    })

    @get:Bindable
    var showPhotoButton by bindDelegate(!showPhoto)

    @get:Bindable
    var showPhotoText by bindDelegate(languageService.getString(R.string.collect_add_text))

    @get:Bindable
    var exclude by bindDelegate(false)

    @get:Bindable
    var showExclude by bindDelegate(!isLandmark)

    @get:Bindable
    var excludeText by bindDelegate(languageService.getString(R.string.collect_exclude))

    @get:Bindable
    var notesError by bindDelegate<String?>(null)

    @get:Bindable
    var notesHint by bindDelegate(languageService.getString(R.string.collect_notes_hint))

    @get:Bindable
    var notes by bindDelegate<String?>(null)

    @get:Bindable
    var notesErrorEnabled by bindDelegate(false)

    @get:Bindable
    var saveButtonText by bindDelegate(
            if (isLandmark) {
                languageService.getString(R.string.collect_save_landmark)
            } else {
                if (isValid) {
                    enumerationSubject.toUpperCase()
                } else {
                    languageService.getString(R.string.collect_save_incomplete)
                }
            }
    )

    @get:Bindable
    var saveButtonTextColor by bindDelegate(
            if (isValid) {
                saveButtonEnabledTextColor
            } else {
                saveButtonDisabledTextColor
            }
    )

    @get:Bindable
    var saveButtonBackground by bindDelegate(
            if (isValid) {
                saveButtonEnabledColor
            } else {
                saveButtonDisabledColor
            }
    )

    @get:Bindable
    var photoText by bindDelegate(languageService.getString(R.string.collect_add_text))

    @get:Bindable
    var landmarkName: String? by bindDelegate<String?>(null, { _, _ ->
        if (isValid) {
            saveButtonBackground = saveButtonEnabledColor
            saveButtonTextColor = saveButtonEnabledTextColor
        } else {
            saveButtonBackground = saveButtonDisabledColor
            saveButtonTextColor = saveButtonDisabledTextColor
        }
    })

    @get:Bindable
    var landmarkHint by bindDelegate(languageService.getString(R.string.collect_landmark_name_hint))

    @get:Bindable
    var landmarkError by bindDelegate<String?>(null)

    @get:Bindable
    var landmarkErrorEnabled by bindDelegate(false)

    @get:Bindable
    var landmarkImage by bindDelegate<String?>(null)

    @get:Bindable
    var gpsDisplay by bindDelegate(languageService.getString(R.string.collect_gps_waiting))

    private val customFieldViewModels = mutableListOf<AbstractCustomViewModel>()

    private val isSufficientlyPrecise
        get() = (location?.accuracy ?: 9999.0f < gpsMinimumPrecision.toFloat())

    private val isValid
        get() =
            if (isLandmark) {
                isSufficientlyPrecise && (landmarkName?.isNotBlank() == true)
            } else {
                isSufficientlyPrecise &&
                        customFieldViewModels.filter {
                            it.customField.isRequired || it.customField.isAutomatic
                        }.fold(true, { acc, next ->
                            acc && next.value != null
                        })
            }

    var gpsVm = CollectGpsPrecisionViewModel(
            minimumPrecision,
            preferredPrecision,
            lowestColor,
            mediumColor,
            highestColor)

    var location: Location? = null

    var googleMap: GoogleMap? = null

    var selectedLandmark: LandmarkType? = null
        set(value) {
            field = value
            landmarkImage = value?.iconLocation
        }

    var isCopied = false

    private val landmarkItem: LandmarkItem
        get() = LandmarkItem(
                LatLng(location!!.latitude, location!!.longitude),
                selectedLandmark!!,
                landmarkName,
                notes)

    private val enumerationItem: EnumerationItem
        get() = EnumerationItem(
                LatLng(location!!.latitude, location!!.longitude),
                location!!.accuracy.toDouble(),
                isCopied,
                customFieldViewModels.filter {
                    it.customField.isRequired
                }.map {
                    it.value != null
                }.fold(true) { acc, b ->
                    acc && b
                },
                exclude,
                notes,
                customFieldViewModels.mapNotNull {
                    if (it.value != null) {
                        it.customField.customKey to
                                CustomFieldDataItem(
                                        it.customField.type,
                                        it.customField.isPrimary,
                                        it.value!!
                                )
                    } else {
                        null
                    }
                }.toMap()
        )

    init {
        mapObservable.subscribe {
            googleMap = it
            @SuppressLint("MissingPermission")
            it.isMyLocationEnabled = true
            it.mapType = GoogleMap.MAP_TYPE_SATELLITE
            location?.let { location ->
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 18.0f))
            }
        }
        locationObservable.subscribe {
            if (it.accuracy < (location?.accuracy ?: 1000.0f)) {
                location = it
                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
                gpsDisplay = "%.5f ".format(it.latitude) + ", %.5f".format(it.longitude)
                gpsVm.precision.set(it.accuracy.toDouble())
            }
        }
    }

    fun addCustomFieldViewModel(viewModel: AbstractCustomViewModel) {
        customFieldViewModels.add(viewModel)
    }

    fun saveItem() {
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
