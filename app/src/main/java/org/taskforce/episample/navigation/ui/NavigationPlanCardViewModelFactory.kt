package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.UserSettings

class NavigationPlanCardViewModelFactory(private val application: Application,
                                         private val userSettings: UserSettings?,
                                         private val lastKnownLocation: LiveData<Pair<LatLng, Float>>,
                                         private val lowestColor: Int,
                                         private val mediumColor: Int,
                                         private val highestColor: Int,
                                         private val viewPhoto: (String?) -> Unit) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NavigationPlanCardViewModel(application,
                userSettings,
                lastKnownLocation,
                lowestColor,
                mediumColor,
                highestColor,
                viewPhoto) as T
    }
}
