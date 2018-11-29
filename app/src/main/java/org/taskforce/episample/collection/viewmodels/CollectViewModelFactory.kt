package org.taskforce.episample.collection.viewmodels

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import io.reactivex.Observable
import io.reactivex.Single
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.core.interfaces.EnumerationSubject

class CollectViewModelFactory(private val application: Application,
                              private val addPoint: (Boolean) -> Unit,
                              private val back: () -> Unit) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CollectViewModel(application,
                addPoint,
                back) as T
    }
}