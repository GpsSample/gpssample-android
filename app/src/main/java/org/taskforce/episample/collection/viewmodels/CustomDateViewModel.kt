package org.taskforce.episample.collection.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.databinding.Bindable
import android.view.View
import io.reactivex.Single
import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.db.config.customfield.CustomDateType
import org.taskforce.episample.db.config.customfield.metadata.DateMetadata
import org.taskforce.episample.utils.bindDelegate
import java.util.*

class CustomDateViewModel(
        customField: CustomField,
        displaySettings: DisplaySettings,
        private val showDatePicker: (CustomField) -> Unit) : AbstractCustomViewModel(customField) {

    @get:Bindable
    var hint by bindDelegate(customField.name)

    @get:Bindable
    var input by bindDelegate<String?>(null)

    override val value = object : MutableLiveData<Date>() {
        override fun setValue(value: Date?) {
            super.setValue(value)

            value?.let { dateValue ->
                input = when ((customField.metadata as? DateMetadata)?.dateType) {
                    CustomDateType.DATE -> displaySettings.getFormattedDate(dateValue, false)
                    CustomDateType.TIME -> displaySettings.getFormattedTime(dateValue)
                    CustomDateType.DATE_TIME -> displaySettings.getFormattedDateWithTime(dateValue, false)
                    null -> ""
                }
            } ?: run { input = "" }
        }
    }

    fun showDatePicker(view: View) {
        showDatePicker(customField)
    }
}