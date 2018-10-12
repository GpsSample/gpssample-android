package org.taskforce.episample.core.mock

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.db.DateRange
import org.taskforce.episample.db.navigation.ResolvedNavigationPlan
import org.taskforce.episample.db.sampling.SampleEntity
import org.taskforce.episample.db.sampling.WarningEntity

class MockCollectManager : CollectManager {
    override fun getNavigationPlans(): LiveData<List<ResolvedNavigationPlan>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNumberOfNavigationPlans(): LiveData<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createNavigationPlans(numberOfNavigationPlansToMake: SampleEntity, numberOfNavigationPlansToMake1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteSamples() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNumberOfEnumerationsInSample(): LiveData<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getWarnings(): LiveData<List<WarningEntity>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSample(): LiveData<SampleEntity?> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNumberOfSamples(): LiveData<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createSample() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getValidEnumerationsDateRange(): LiveData<DateRange> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNumberOfValidEnumerations(): LiveData<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun deleteCollectItem(collectItem: CollectItem) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateLandmark(landmark: Landmark, callback: () -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateEnumerationItem(item: Enumeration, callback: () -> Unit) {

    }

    override fun addEnumerationItem(item: Enumeration, callback: (enumerationId: String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addLandmark(landmark: Landmark, callback: (landmarkId: String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val userSession = LiveUserSession(
            "username",
            false,
            studyId = "ANY",
            configId = "ANY")

    private val enumerations = listOf(
            MockEnumeration.createMockEnumeration(title = "John Doe",
                    location = LatLng(37.4211343, -122.0860752),
                    gpsPrecision = 8.1,
                    note = "No answer at door"),
            MockEnumeration.createMockEnumeration(title = "Jane Doe",
                    location = LatLng(37.422057, -122.0846663),
                    gpsPrecision = 3.2,
                    isIncomplete = true,
                    note = "Rabid dog"),
            MockEnumeration.createMockEnumeration(title = "Joe Doe",
                    location = LatLng(37.422065, -122.0846862),
                    gpsPrecision = 3.2,
                    excluded = true,
                    image = "file:///sdcard/Pictures/profile copy.jpg")
    )

    override fun getEnumerations(): LiveData<List<Enumeration>> {
        return MutableLiveData<List<Enumeration>>().apply {
            postValue(enumerations)
        }
    }

    private val landmarks = listOf(
            MockLandmark.createMockLandmark(title = "Bus stop",
                    location = LatLng(37.4222736, -122.0838697),
                    gpsPrecision = 8.5),
            MockLandmark.createMockLandmark(title = "Large tree",
                    location = LatLng(37.421865, -122.0833879),
                    gpsPrecision = 20.3))

    override fun getLandmarks(): LiveData<List<Landmark>> {
        return MutableLiveData<List<Landmark>>().apply {
            postValue(landmarks)
        }
    }

    private val collectLiveData = LiveDataPair(getEnumerations(), getLandmarks())
    override fun getCollectItems(): LiveData<List<CollectItem>> = Transformations.map(collectLiveData) {
        val enumerations = it.first ?: emptyList()
        val landmarks = it.second ?: emptyList()
        enumerations + landmarks
    }

    private val breadcrumbs: MutableList<Breadcrumb> = mutableListOf(MockBreadcrumb(userSession.username, 5.6, LatLng(37.4218651, -122.083387899), true),
            MockBreadcrumb(userSession.username, 6.2, LatLng(37.42227362, -122.08386971), false),
            MockBreadcrumb(userSession.username, 8.0, LatLng(37.42113431, -122.086075205), false),
            MockBreadcrumb(userSession.username, 1.4, LatLng(37.42205705, -122.084666302), false))
    private val breadCrumbLiveData = MutableLiveData<List<Breadcrumb>>().apply {
        postValue(breadcrumbs)
    }

    override fun getBreadcrumbs(): MutableLiveData<List<Breadcrumb>> {
        return breadCrumbLiveData
    }

    override fun getLandmarkTypes(): List<LandmarkType> {
        val landmarkType = MockLandmarkType.createMockLandmarkType(name = "Nature")
        val defaultType = MockLandmarkType.createMockLandmarkType()
        val othertype = MockLandmarkType.createMockLandmarkType(name = "Other")

        return listOf(defaultType, landmarkType, othertype)
    }

    override fun addBreadcrumb(breadcrumb: Breadcrumb, callback: (breadcrumbId: String) -> Unit) {
        breadcrumbs.add(breadcrumb)
        breadCrumbLiveData.postValue(breadcrumbs)
    }
}