package org.taskforce.episample.collection.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.databinding.Bindable
import io.reactivex.Single
import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.utils.bindDelegate
import java.util.*

class CustomDateViewModel(customField: CustomField,
                          private val showDatePicker: () -> Single<Date>): AbstractCustomViewModel(customField) {

    @get:Bindable
    var hint by bindDelegate(customField.name)

    @get:Bindable
    var input by bindDelegate<String?>(null)

    override val value = object: MutableLiveData<Date>() {
        override fun setValue(value: Date?) {
            super.setValue(value)
            
            input = value.toString()
        }
    }

    fun showDatePicker() {
        showDatePicker.invoke().subscribe({
            value.postValue(it)
        }, {
            value.postValue(null)
        })
    }
}