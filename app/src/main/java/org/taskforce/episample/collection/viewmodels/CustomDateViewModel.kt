package org.taskforce.episample.collection.viewmodels

import android.databinding.Bindable
import io.reactivex.Single
import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.utils.bindDelegate
import java.util.*

class CustomDateViewModel(customField: CustomField,
                          val showDatePicker: () -> Single<Date>): AbstractCustomViewModel(customField) {

    @get:Bindable
    var hint by bindDelegate(customField.name)

    @get:Bindable
    var input by bindDelegate<String?>(null)

    override var value: Date? = null
        set(value) {
            field = value
            input = value.toString()
        }

    fun showDatePicker() {
        showDatePicker.invoke().subscribe({
            value = it
        }, {
            value = null
        })
    }
}