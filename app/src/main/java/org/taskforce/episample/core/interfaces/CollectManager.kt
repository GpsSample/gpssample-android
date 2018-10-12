package org.taskforce.episample.core.interfaces

import android.arch.lifecycle.LiveData
import org.taskforce.episample.db.DateRange
import org.taskforce.episample.db.sampling.SampleEntity
import org.taskforce.episample.db.sampling.WarningEntity

interface CollectManager {
    val userSession: UserSession

    fun getLandmarkTypes(): List<LandmarkType>

    fun getEnumerations(): LiveData<List<Enumeration>>
    fun getLandmarks(): LiveData<List<Landmark>>
    fun getCollectItems(): LiveData<List<CollectItem>>
    fun getBreadcrumbs(): LiveData<List<Breadcrumb>>
    fun getNumberOfValidEnumerations(): LiveData<Int>

    fun addBreadcrumb(breadcrumb: Breadcrumb, callback: (breadcrumbId: String) -> Unit)
    fun addEnumerationItem(item: Enumeration, callback: (enumerationId: String) -> Unit)
    fun addLandmark(landmark: Landmark, callback: (landmarkId: String) -> Unit)
    
    fun updateEnumerationItem(item: Enumeration, callback: () -> Unit)
    fun updateLandmark(landmark: Landmark, callback: () -> Unit)
    fun getValidEnumerationsDateRange(): LiveData<DateRange>
    fun createSample()
    fun getNumberOfSamples(): LiveData<Int>
    fun getWarnings(): LiveData<List<WarningEntity>>
    fun getSample(): LiveData<SampleEntity?>
    fun getNumberOfEnumerationsInSample(): LiveData<Int>
    fun deleteSamples()
    fun createNavigationPlans(numberOfNavigationPlansToMake: SampleEntity, numberOfNavigationPlansToMake1: Int)
}
