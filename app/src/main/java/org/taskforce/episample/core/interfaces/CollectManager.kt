package org.taskforce.episample.core.interfaces

import android.arch.lifecycle.LiveData

interface CollectManager {
    val userSession: UserSession
    
    fun getEnumerations(): LiveData<List<Enumeration>>
    fun getLandmarks(): LiveData<List<Landmark>>
    fun getCollectItems(): LiveData<List<CollectItem>>
    fun getBreadcrumbs(): LiveData<List<Breadcrumb>>
    fun getLandmarkTypes(): LiveData<List<LandmarkType>>

    fun addBreadcrumb(breadcrumb: Breadcrumb, callback: (breadcrumbId: String) -> Unit)
    fun addEnumerationItem(item: Enumeration, callback: (enumerationId: String) -> Unit)
    fun addLandmark(landmark: Landmark, callback: (landmarkId: String) -> Unit)
    
    fun updateEnumerationItem(item: Enumeration, callback: () -> Unit)
    fun updateLandmark(landmark: Landmark, callback: () -> Unit)
}
