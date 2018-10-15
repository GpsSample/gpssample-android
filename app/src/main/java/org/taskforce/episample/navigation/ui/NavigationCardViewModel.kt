package org.taskforce.episample.navigation.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.text.SpannableString
import android.view.View
import org.taskforce.episample.collection.ui.CollectGpsPrecisionViewModel
import org.taskforce.episample.core.interfaces.CollectItem

interface NavigationCardViewModel {
    val visibility: MutableLiveData<Boolean>
    val buttonLayoutVisibility: MutableLiveData<Boolean>
    val itemData: MutableLiveData<CollectItem>
    val navigationStatus: LiveData<String>
    val title: LiveData<String?>
    val distance: LiveData<String>
    val gpsPrecisionVm: CollectGpsPrecisionViewModel
    val imageUrl: LiveData<String?>
    val showDetailsText: LiveData<Boolean>
    val detailsText: LiveData<SpannableString>
    val primaryButtonText: LiveData<String?>
    val secondaryButtonText: LiveData<String?>
    fun primaryButtonAction(view: View) {}
    fun secondaryButtonAction(view: View) {}
    fun viewPhotoAction(view: View) {}
}