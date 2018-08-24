package org.taskforce.episample.core.interfaces

import android.arch.lifecycle.LiveData

interface CollectManager {
    val studyId: String
    val configId: String

    fun getEnumerations(): LiveData<List<Enumeration>>
    fun getLandmarks(): LiveData<List<Landmark>>
    fun getCollectItems(): LiveData<List<CollectItem>>
    fun getBreadcrumbs(): LiveData<List<Breadcrumb>>
    fun getEnumerationSubject(): LiveData<EnumerationSubject>
    fun getLandmarkTypes(): LiveData<List<LandmarkType>>
    fun getUserSettings(): LiveData<UserSettings>
    fun getDisplaySettings(): LiveData<DisplaySettings>
    
    fun addBreadcrumb(breadcrumb: Breadcrumb)
    fun addEnumerationItem(item: Enumeration)
    fun addLandmark(landmark: Landmark)
    
    fun updateEnumerationItem(item: Enumeration)
    fun updateLandmark(landmark: Landmark)
}
