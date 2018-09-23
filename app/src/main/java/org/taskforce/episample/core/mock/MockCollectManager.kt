package org.taskforce.episample.core.mock

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.core.interfaces.Enumeration
import org.taskforce.episample.db.config.customfield.CustomFieldType
import java.util.*

class MockCollectManager : CollectManager {
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

    private val breadcrumbs: MutableList<Breadcrumb> = mutableListOf(MockBreadcrumb(5.6, LatLng(37.4218651, -122.083387899)),
            MockBreadcrumb(6.2, LatLng(37.42227362, -122.08386971)),
            MockBreadcrumb(8.0, LatLng(37.42113431, -122.086075205)),
            MockBreadcrumb(1.4, LatLng(37.42205705, -122.084666302)))
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