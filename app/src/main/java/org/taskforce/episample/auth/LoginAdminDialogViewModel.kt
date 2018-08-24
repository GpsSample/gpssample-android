package org.taskforce.episample.auth

import android.databinding.BaseObservable
import android.databinding.Bindable
import org.taskforce.episample.utils.bindDelegate

class LoginAdminDialogViewModel(
        private val password: String,
        val title: String,
        val hint: String,
        val cancel: String,
        val done: String,
        val error: String,
        val onCancel: () -> Unit,
        val onDone: () -> Unit) : BaseObservable() {

    @get:Bindable
    var errorEnabled by bindDelegate(false)

    @get:Bindable
    var input by bindDelegate<String?>(null)

    fun cancel() {
        onCancel()
    }

    fun done() {
        if (password == input) {
            onDone()
            cancel()
        }
        else {
            errorEnabled = true
        }
    }
}