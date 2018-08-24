package org.taskforce.episample.config.landmark

import android.databinding.BaseObservable
import android.databinding.Bindable
import io.reactivex.Observable
import org.taskforce.episample.R
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.fileImport.models.LandmarkType
import org.taskforce.episample.utils.bindDelegate

class LandmarkAddViewModel(
        val languageService: LanguageService,
        saveColorRes: Int,
        saveColorDisabledRes: Int,
        selectedObservable: Observable<String>,
        private val landmarkTypeManager: LandmarkTypeManager,
        val close: () -> Unit) :
        BaseObservable() {

    init {
        languageService.update = {
            title = it.getString(R.string.config_landmarks_add_title)
            save = it.getString(R.string.config_add_save)
            hint = it.getString(R.string.config_landmarks_add_hint)
            landmarkIconTitle = it.getString(R.string.config_landmarks_add_icon_title)
        }
    }

    var landmarkTypeToEdit: LandmarkType? = null

    fun loadLandmarkTypeToEdit(landmarkType: LandmarkType?) {
        landmarkTypeToEdit = landmarkType

        landmarkName = landmarkTypeToEdit?.name
        iconLocation = landmarkTypeToEdit?.iconLocation
    }

    @get:Bindable
    private var iconLocation: String? by bindDelegate<String?>(null, { _, newValue ->
        saveEnabled = checkValidity(landmarkName, newValue)
    })

    @get:Bindable
    var landmarkName: String? by bindDelegate<String?>(null, { _, newValue ->
        saveEnabled = checkValidity(newValue, iconLocation)
    })

    init {
        selectedObservable.subscribe {
            iconLocation = it
        }
    }

    @get:Bindable
    var title by bindDelegate(languageService.getString(R.string.config_landmarks_add_title))

    @get:Bindable
    var save by bindDelegate(languageService.getString(R.string.config_add_save))

    @get:Bindable
    var saveColor by bindDelegate(saveColorDisabledRes)

    @get:Bindable
    private var saveEnabled by bindDelegate(false, { _, newValue ->
        if (newValue) {
            saveColor = saveColorRes
        } else {
            saveColor = saveColorDisabledRes
        }
    })

    @get:Bindable
    var error by bindDelegate<String?>(null, { _, newValue ->
        errorEnabled = newValue?.isBlank() == false
    })

    @get:Bindable
    var errorEnabled by bindDelegate(false)

    @get:Bindable
    var hint by bindDelegate(languageService.getString(R.string.config_landmarks_add_hint))

    @get:Bindable
    var landmarkIconTitle by bindDelegate(languageService.getString(R.string.config_landmarks_add_icon_title))

    fun save() {
        if (saveEnabled) {
            val landmark = LandmarkType(landmarkName!!, iconLocation!!)
            if (landmarkTypeToEdit != null) {
                landmark.id = landmarkTypeToEdit!!.id
                landmarkTypeManager.editLandmarkType(landmark)
            } else {
                landmarkTypeManager.addLandmarkType(landmark)
            }
            close()
        } else {
            error = languageService.getString(R.string.config_landmarks_add_error)
        }
    }

    private fun checkValidity(name: String?, iconLocation: String?) =
            name?.isBlank() == false && iconLocation?.isBlank() == false
}