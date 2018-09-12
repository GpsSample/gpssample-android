package org.taskforce.episample.core.interfaces

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.mock.MockBreadcrumb
import org.taskforce.episample.core.mock.MockLandmark
import org.taskforce.episample.core.mock.MockLandmarkType
import org.taskforce.episample.core.navigation.SurveyStatus
import java.util.*

interface NavigationManager {
    val userSession: UserSession

    fun getNavigationItems(): LiveData<List<NavigationItem>>
    fun getLandmarks(): LiveData<List<Landmark>>
    fun getCollectItems(): LiveData<List<CollectItem>>
    fun getBreadcrumbs(): LiveData<List<Breadcrumb>>
    fun getPossiblePath(): LiveData<List<Breadcrumb>>
    fun getLandmarkTypes(): LiveData<List<LandmarkType>>
    fun addBreadcrumb(breadcrumb: Breadcrumb, callback: (breadcrumbId: String) -> Unit)
    fun skipNavigationItem(navigationItemId: String, skipReason: String)
}

data class MockNavigationItem(override val title: String,
                              override val navigationOrder: Int,
                              override var surveyStatus: SurveyStatus,
                              override val location: LatLng,
                              override val gpsPrecision: Double,
                              override val isIncomplete: Boolean,
                              override val isExcluded: Boolean,
                              override val image: String?,
                              override val note: String?,
                              override val id: String?,
                              override val dateCreated: Date = Date()) : NavigationItem {
    override val customFieldValues: List<CustomFieldValue> = listOf()


    override val displayDate: String = "TODO"

    companion object {
        fun createMockNavigationItem(title: String,
                                     navigationOrder: Int,
                                     surveyStatus: SurveyStatus,
                                     location: LatLng = LatLng(37.4211343, -122.0860752),
                                     gpsPrecision: Double = 0.0,
                                     isIncomplete: Boolean = false,
                                     excluded: Boolean = false,
                                     image: String? = null,
                                     note: String? = null,
                                     dateCreated: Date = Date()): MockNavigationItem {
            return MockNavigationItem(title, navigationOrder, surveyStatus, location, gpsPrecision, isIncomplete, excluded, image, note, UUID.randomUUID().toString(), dateCreated)
        }
    }
}

class MockNavigationManager : NavigationManager {

    private val navigationItems = listOf(
            MockNavigationItem.createMockNavigationItem(title = "John Doe",
                    navigationOrder = 1,
                    surveyStatus = SurveyStatus.Skipped("Skipped Reason"),
                    location = LatLng(37.4211343, -122.0860752),
                    gpsPrecision = 8.1,
                    note = "No answer at door"),
            MockNavigationItem.createMockNavigationItem(title = "Jane Doe",
                    surveyStatus = SurveyStatus.Incomplete(),
                    navigationOrder = 2,
                    location = LatLng(37.422057, -122.1446663),
                    gpsPrecision = 3.2,
                    isIncomplete = true,
                    note = "Rabid dog"),
            MockNavigationItem.createMockNavigationItem(title = "Jane Doe",
                    surveyStatus = SurveyStatus.Skipped("Skipped Reason 2"),
                    navigationOrder = 3,
                    location = LatLng(37.422057, -122.1446663),
                    gpsPrecision = 3.2,
                    isIncomplete = true,
                    note = "Rabid dog"),
            MockNavigationItem.createMockNavigationItem(title = "Joe Doe",
                    surveyStatus = SurveyStatus.Complete(),
                    location = LatLng(37.422065, -122.0846862),
                    navigationOrder = 4,
                    gpsPrecision = 3.2,
                    excluded = true,
                    image = "file:///sdcard/Pictures/profile copy.jpg"),
            MockNavigationItem.createMockNavigationItem(title = "John Doe",
                    navigationOrder = 5,
                    surveyStatus = SurveyStatus.Problem("Problem description"),
                    location = LatLng(37.4211343, -122.1060752),
                    gpsPrecision = 8.1,
                    note = "No answer at door")
            )

    private val landmarks = listOf(
            MockLandmark.createMockLandmark(title = "Bus stop",
                    location = LatLng(37.4222736, -122.0838697),
                    gpsPrecision = 8.5),
            MockLandmark.createMockLandmark(title = "Large tree",
                    location = LatLng(37.421865, -122.0833879),
                    gpsPrecision = 20.3))

    private val liveNavigationItems = MutableLiveData<List<NavigationItem>>().apply {
        value = navigationItems
    }

    private val liveLandmarks = MutableLiveData<List<Landmark>>().apply {
        value = landmarks
    }

    private val breadcrumbs: MutableList<Breadcrumb> = mutableListOf(MockBreadcrumb(5.6, LatLng(37.4218651, -122.083387899)),
            MockBreadcrumb(6.2, LatLng(37.42327362, -122.08396971)),
            MockBreadcrumb(8.0, LatLng(37.42213431, -122.086175205)),
            MockBreadcrumb(1.4, LatLng(37.42305705, -122.084766302)))
    private val liveBreadcrumbs = MutableLiveData<List<Breadcrumb>>().apply {
        value = breadcrumbs
    }

    private val possiblePath: MutableList<Breadcrumb> = mutableListOf(MockBreadcrumb(5.6, LatLng(37.4218651, -122.083387899)),
            MockBreadcrumb(6.2, LatLng(37.42327362, -122.08396971)),
            MockBreadcrumb(8.0, LatLng(37.42213431, -122.086175205)),
            MockBreadcrumb(1.4, LatLng(37.42305705, -122.084766302)))
    private val livePossiblePath = MutableLiveData<List<Breadcrumb>>().apply {
        value = possiblePath
    }

    override val userSession: UserSession
        get() = LiveUserSession("Jesse", false, "ANY", "ANY")

    override fun getNavigationItems(): LiveData<List<NavigationItem>> {
        return liveNavigationItems
    }

    override fun getLandmarks(): LiveData<List<Landmark>> {
        return liveLandmarks
    }

    private val collectLiveData = LiveDataPair(getNavigationItems(), getLandmarks())
    override fun getCollectItems(): LiveData<List<CollectItem>> = Transformations.map(collectLiveData) {
        val enumerations = it.first
        val landmarks = it.second
        enumerations + landmarks
    }

    override fun getBreadcrumbs(): LiveData<List<Breadcrumb>> {
        return liveBreadcrumbs
    }

    override fun getPossiblePath(): LiveData<List<Breadcrumb>> {
        return livePossiblePath
    }

    override fun getLandmarkTypes(): LiveData<List<LandmarkType>> {
        return MutableLiveData<List<LandmarkType>>().apply {
            val landmarkType = MockLandmarkType.createMockLandmarkType(name = "Nature")
            val defaultType = MockLandmarkType.createMockLandmarkType()
            val othertype = MockLandmarkType.createMockLandmarkType(name = "Other")

            value = listOf(defaultType, landmarkType, othertype)
        }
    }

    override fun addBreadcrumb(breadcrumb: Breadcrumb, callback: (breadcrumbId: String) -> Unit) {
        breadcrumbs.add(breadcrumb)
        liveBreadcrumbs.postValue(breadcrumbs)
    }

    override fun skipNavigationItem(navigationItemId: String, skipReason: String) {
        navigationItems.firstOrNull { it.id == navigationItemId }?.let {
            it.surveyStatus = SurveyStatus.Skipped(skipReason)
            liveNavigationItems.postValue(navigationItems)
        }

    }
}