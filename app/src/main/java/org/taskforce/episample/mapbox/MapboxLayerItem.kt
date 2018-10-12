package org.taskforce.episample.mapbox

import android.databinding.BaseObservable
import android.widget.CompoundButton

class MapboxLayerItem(val name: String, var isChecked: Boolean, val settingUpdated: (Boolean) -> Unit): BaseObservable() {
    fun onCheckChanged(buttonView: CompoundButton, isChecked: Boolean) {
        settingUpdated(isChecked)
    }
}