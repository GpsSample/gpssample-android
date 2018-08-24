package org.taskforce.episample.collection.viewmodels

import android.databinding.Bindable
import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.utils.bindDelegate

class CustomCheckboxViewModel(customField: CustomField): AbstractCustomViewModel(customField) {

    @get:Bindable
    var checked by bindDelegate(false)

    @get:Bindable
    var title by bindDelegate(customField.name)

    override val value: Boolean
        get() = checked

}