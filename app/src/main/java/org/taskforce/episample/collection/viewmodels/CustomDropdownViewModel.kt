package org.taskforce.episample.collection.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.databinding.Bindable
import android.view.View
import android.widget.ArrayAdapter
import org.taskforce.episample.config.fields.CustomDropdown
import org.taskforce.episample.config.fields.CustomFieldTypeConstants
import org.taskforce.episample.core.interfaces.CustomField

class CustomDropdownViewModel(
        customField: CustomField,
        context: Context,
        private val provideSelectedItem: (ArrayAdapter<String>, View) -> String): AbstractCustomViewModel(customField) {

    var view: View? = null

    @get:Bindable
    val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, listOf("TODO"))
//    val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
////            (customField.properties[CustomFieldTypeConstants.DROPDOWN_ITEMS] as List<*>).mapNotNull {
////                if (it is CustomDropdown) {
////                    it.value
////                }
////                else {
////                    null
////                }
////            })
//
    override val value = MutableLiveData<String>().apply { value = "TODO" }
//        get() = view?.let {
////            provideSelectedItem.invoke(adapter, it)
//        } ?: "Unknown"
}