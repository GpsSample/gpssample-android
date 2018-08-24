package org.taskforce.episample.collection.viewmodels

import android.databinding.Bindable
import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.utils.bindDelegate

class CustomTextViewModel(customField: CustomField): AbstractCustomViewModel(customField) {

    @get:Bindable
    var hint by bindDelegate(customField.name)

    @get:Bindable
    var input by bindDelegate<String?>(null)

    override val value: String?
        get() = input

}