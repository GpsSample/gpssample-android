package org.taskforce.episample.config.geography

import android.view.View

class EnumerationAreaViewModel(
        private val enumerationArea: EnumerationArea,
        val depth: Int,
        val enumerationAreaActionListener: EnumerationActionCallbacks) {

    val name: String
        get() = enumerationArea.name

    fun onEditClicked(view: View) {
        //TODO in TFFGH-284
    }

    fun onDeleteClicked(view: View) {
        enumerationAreaActionListener.onDelete(enumerationArea)
    }

    interface EnumerationActionCallbacks {
        fun onDelete(enumerationArea: EnumerationArea)
    }
}