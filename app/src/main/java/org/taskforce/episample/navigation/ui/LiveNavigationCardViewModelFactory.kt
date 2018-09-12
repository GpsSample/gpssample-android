package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.UserSettings

class LiveNavigationCardViewModelFactory(private val application: Application,
                                         private val userSettings: UserSettings?,
                                         private val lastKnownLocation: LiveData<Pair<LatLng, Float>>,
                                         private val lowestColor: Int,
                                         private val mediumColor: Int,
                                         private val highestColor: Int,
                                         private val launchSurvey: () -> Unit,
                                         private val showSkipDialog: () -> Unit) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LiveNavigationCardViewModel(application,
                userSettings,
                lastKnownLocation,
                lowestColor,
                mediumColor,
                highestColor,
                launchSurvey,
                showSkipDialog) as T
    }
}