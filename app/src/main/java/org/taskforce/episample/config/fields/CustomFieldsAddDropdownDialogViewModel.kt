package org.taskforce.episample.config.fields

import android.databinding.BaseObservable
import android.databinding.Bindable
import org.taskforce.episample.utils.bindDelegate

class CustomFieldsAddDropdownDialogViewModel(
        val title: String,
        val hint: String,
        val cancel: String,
        val done: String,
        private val dismiss: () -> Unit) : BaseObservable() {

    @get:Bindable
    var key by bindDelegate<String?>(null)

    fun cancel() {
        dismiss()
    }

    fun done() {
        cancel()
    }
}