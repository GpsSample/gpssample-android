package org.taskforce.episample.collection.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableField
import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.db.config.customfield.metadata.NumberMetadata

class CustomNumberViewModel(customField: CustomField): AbstractCustomViewModel(customField) {
    
    var hint = object : ObservableField<String>("") {
        override fun get(): String? {
            var value = customField.name
            if (customField.isRequired) {
                value += " *"
            }
            return value
        }
    }

    var input = object: ObservableField<String?>("") {
        override fun set(newValue: String?) {
            super.set(newValue)
            
            value.postValue(newValue)
        }
    }
    
    var isInteger = (customField.metadata as NumberMetadata).isIntegerOnly

    override val value = MutableLiveData<String?>()

}