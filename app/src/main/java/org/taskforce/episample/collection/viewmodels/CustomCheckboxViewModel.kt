package org.taskforce.episample.collection.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableField
import org.taskforce.episample.core.interfaces.CustomField

class CustomCheckboxViewModel(customField: CustomField): AbstractCustomViewModel(customField) {

    var checked = object : ObservableField<Boolean>(false) {
        override fun set(newValue: Boolean?) {
            super.set(newValue)
            
            value.postValue(newValue)
        }
    }
    
    var title = object : ObservableField<String>("") {
        override fun get(): String? {
            var value = customField.name
            if (customField.isRequired) {
                value += " *"
            }
            return value
        }
    }

    override val value = MutableLiveData<Boolean>()

}