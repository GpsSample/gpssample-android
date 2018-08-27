package org.taskforce.episample.db.config

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.core.interfaces.UserSettings
import org.taskforce.episample.core.mock.MockBreadcrumb
import org.taskforce.episample.core.mock.MockCollectManager
import org.taskforce.episample.db.ConfigRepository

class IntegrationCollectManager(application: Application,
                                override val configId: String,
                                override val studyId: String): CollectManager {
    val configRepository = ConfigRepository(application)

    val mockCollectManager = MockCollectManager()

    override fun getEnumerations(): LiveData<List<org.taskforce.episample.core.interfaces.Enumeration>> {
        return Transformations.map(configRepository.getEnumerations(studyId), {
            return@map it?.map { Enumeration(it) as org.taskforce.episample.core.interfaces.Enumeration }
        })
    }

    class Enumeration(dbEnumeration: org.taskforce.episample.db.collect.Enumeration): org.taskforce.episample.core.interfaces.Enumeration {

        override val image = dbEnumeration.image
        override val isIncomplete = dbEnumeration.isIncomplete
        override val excluded = dbEnumeration.isExcluded
        override val title = dbEnumeration.title
        override val note = dbEnumeration.note
        override val location = LatLng(dbEnumeration.lat, dbEnumeration.lng)
        override val gpsPrecision = dbEnumeration.gpsPrecision
        override val dateCreated = dbEnumeration.dateCreated
        override val displayDate = "TODO"
        override val customFieldValues: List<CustomFieldValue> = listOf()
    }
    override fun getLandmarks(): LiveData<List<Landmark>> {
        return mockCollectManager.getLandmarks()
    }

    override fun getCollectItems(): LiveData<List<CollectItem>> {
        return mockCollectManager.getCollectItems()
    }

    override fun getBreadcrumbs(): LiveData<List<Breadcrumb>> {
        return mockCollectManager.getBreadcrumbs()
    }

    override fun getEnumerationSubject(): LiveData<org.taskforce.episample.core.interfaces.EnumerationSubject> {
        val resolvedConfigData = configRepository.getResolvedConfig(configId)
        return Transformations.map(resolvedConfigData, {
            val resolvedConfig = it
            val enumerationSubject = resolvedConfig.enumerationSubject
            return@map enumerationSubject
        })
    }

    override fun getLandmarkTypes(): LiveData<List<LandmarkType>> {
        return mockCollectManager.getLandmarkTypes()
    }

    override fun getUserSettings(): LiveData<UserSettings> {
        return Transformations.map(configRepository.getResolvedConfig(configId), {
            return@map it.userSettings
        })
    }

    override fun getDisplaySettings(): LiveData<DisplaySettings> {
        return mockCollectManager.getDisplaySettings()
    }

    override fun addBreadcrumb(breadcrumb: Breadcrumb) {
        mockCollectManager.addBreadcrumb(breadcrumb)
    }

    override fun updateEnumerationItem(item: org.taskforce.episample.core.interfaces.Enumeration) {
        mockCollectManager.updateEnumerationItem(item)
    }

    override fun updateLandmark(landmark: Landmark) {
        mockCollectManager.updateLandmark(landmark)
    }

    override fun addEnumerationItem(item: org.taskforce.episample.core.interfaces.Enumeration) {
        mockCollectManager.addEnumerationItem(item)
    }

    override fun addLandmark(landmark: Landmark) {
        mockCollectManager.addLandmark(landmark)
    }
}
