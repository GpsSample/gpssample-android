package org.taskforce.episample.config.geography

import android.databinding.BaseObservable
import android.databinding.Bindable
import org.taskforce.episample.R
import org.taskforce.episample.BR
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.utils.bindDelegate

class GeographyDialogViewModel(
        languageService: LanguageService,
        val disabledColor: Int,
        val enabledColor: Int,
        val cancel: () -> Unit,
        private val quickstartReceiver: QuickstartReceiver) : BaseObservable() {

    init {
        languageService.update = {
            title = languageService.getString(R.string.config_geography_quickstart)
            latitudeHint = languageService.getString(R.string.config_geography_dialog_latitude_hint)
            longitudeHint = languageService.getString(R.string.config_geography_dialog_longitude_hint)
            radiusHint = languageService.getString(R.string.config_geography_dialog_radius_hint)
            cancelText = languageService.getString(R.string.config_geography_dialog_cancel)
            area = languageService.getString(R.string.config_geography_dialog_get_area)
        }
    }

    @Bindable
    var submitEnabled: Boolean = false
        get() = (latitude?.isNotBlank() == true &&
                longitude?.isNotBlank() == true &&
                radius?.isNotBlank() == true).apply {
            submitColor = if (this) {
                enabledColor
            } else {
                disabledColor
            }
        }

    @get:Bindable
    var submitColor by bindDelegate(disabledColor)

    @get:Bindable
    var title by bindDelegate(languageService.getString(R.string.config_geography_dialog_title))

    @get:Bindable
    var latitudeHint by bindDelegate(languageService.getString(R.string.config_geography_dialog_latitude_hint))

    @get:Bindable
    var longitudeHint by bindDelegate(languageService.getString(R.string.config_geography_dialog_longitude_hint))

    @get:Bindable
    var radiusHint by bindDelegate(languageService.getString(R.string.config_geography_dialog_radius_hint))

    @get:Bindable
    var cancelText by bindDelegate(languageService.getString(R.string.config_geography_dialog_cancel))

    @get:Bindable
    var area by bindDelegate(languageService.getString(R.string.config_geography_dialog_get_area))

    @get:Bindable
    var latitude: String? by bindDelegate<String?>(null, { _, _ ->
        notifyPropertyChanged(BR.submitEnabled)
    })

    @get:Bindable
    var longitude: String? by bindDelegate<String?>(null, { _, _ ->
        notifyPropertyChanged(BR.submitEnabled)
    })

    @get:Bindable
    var radius: String? by bindDelegate<String?>(null, { _, _ ->
        notifyPropertyChanged(BR.submitEnabled)
    })

    fun generateArea() {
        quickstartReceiver.quickstartData(latitude?.toDouble() ?: 0.0, longitude?.toDouble()
                ?: 0.0, radius?.toDouble() ?: 1.0)
        cancel()
    }
}

interface QuickstartReceiver {
    fun quickstartData(latitude: Double, longitude: Double, radius: Double)
}