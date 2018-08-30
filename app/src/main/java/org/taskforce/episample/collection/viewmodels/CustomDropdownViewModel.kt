package org.taskforce.episample.collection.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.databinding.Bindable
import android.databinding.ObservableField
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.taskforce.episample.core.interfaces.CustomField
import org.taskforce.episample.db.config.customfield.metadata.CustomDropdown
import org.taskforce.episample.db.config.customfield.metadata.DropdownMetadata

class CustomDropdownViewModel(
        customField: CustomField,
        context: Context,
        private val provideSelectedItem: (ArrayAdapter<String>, View) -> CustomDropdown?) : AbstractCustomViewModel(customField) {

    var view: View? = null
    
    val items = ((customField.metadata as DropdownMetadata).items).mapNotNull {
        it.value
    }.toMutableList()
    
    init {
        items.add(0, "Select an option")
    }
    
    @get:Bindable
    val fieldName = object: ObservableField<String>("") {
        override fun get(): String? {
            var name = customField.name
            if (customField.isRequired) {
                name = "$name *"
            }
            return name
        }
    }

    @get:Bindable
    val adapter = object: ArrayAdapter<String>(context, 
            android.R.layout.simple_spinner_dropdown_item,
            items) {
        
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getDropDownView(position, convertView, parent)
            val textView = view as TextView
            if (position == 0) {
                textView.setTextColor(Color.GRAY)
            } else {
                textView.setTextColor(Color.BLACK)
            }
            return view
        }
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getDropDownView(position, convertView, parent)
            val textView = view as TextView
            if (position == 0) {
                textView.setTextColor(Color.GRAY)
            } else {
                textView.setTextColor(Color.BLACK)
            }
            return view
        }
    }

    override val value = object : MutableLiveData<CustomDropdown>() {
        override fun getValue() = view?.let {
            provideSelectedItem.invoke(adapter, it)
        }
    }
}
