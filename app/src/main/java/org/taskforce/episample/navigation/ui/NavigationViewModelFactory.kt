package org.taskforce.episample.navigation.ui

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.Single
import org.taskforce.episample.config.language.LanguageService

class NavigationViewModelFactory(private val application: Application,
                                 private val languageService: LanguageService,
                                 private val addLandmark: () -> Unit,
                                 private val back: () -> Unit) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NavigationViewModel(application,
                languageService,
                addLandmark,
                back) as T
    }
}