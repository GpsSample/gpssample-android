package org.taskforce.episample.db.config

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.utils.toDBBreadcrumb
import org.taskforce.episample.utils.toDBEnumeration
import org.taskforce.episample.utils.toDBLandmark

class LiveCollectManager(val application: Application,
                         val configManager: ConfigManager,
                         val studyRepository: StudyRepository,
                         override val userSession: UserSession) : CollectManager {

    val studyId: String
        get() = userSession.studyId
    val configId: String
        get() = userSession.configId

    override fun getEnumerations(): LiveData<List<org.taskforce.episample.core.interfaces.Enumeration>> {
        return Transformations.map(studyRepository.getEnumerations(studyId), {
            return@map it?.map { it.makeLiveEnumeration() }
        })
    }

    val mergedLandmarks = LiveDataPair(
            configManager.getConfig(application),
            studyRepository.getLandmarks(studyId)
    )

    val getLandmarks = Transformations.map(mergedLandmarks, { (config, landmarks) ->
        if (config == null || landmarks == null) {
            return@map listOf<org.taskforce.episample.core.interfaces.Landmark>()
        }

        val resolvedLandmarks: List<org.taskforce.episample.core.interfaces.Landmark> =
                landmarks.map { dbLandmark ->
                    when (dbLandmark.metadata) {
                        is LandmarkTypeMetadata.CustomId -> {
                            val customId = (dbLandmark.metadata as LandmarkTypeMetadata.CustomId).id
                            val matchingType = config.landmarkTypes.filter {
                                (it.metadata as? LandmarkTypeMetadata.CustomId)?.id == customId
                            }.first()


                            return@map dbLandmark.makeLiveLandmark(matchingType) as org.taskforce.episample.core.interfaces.Landmark
                        }
                        is LandmarkTypeMetadata.BuiltInLandmark -> {
                            val landmarkType = config.let {
                                return@let it.landmarkTypes.filter {
                                    (it.metadata as? LandmarkTypeMetadata.BuiltInLandmark)?.type == dbLandmark.metadata.type
                                }.first()
                            }
                            return@map dbLandmark.makeLiveLandmark(landmarkType) as org.taskforce.episample.core.interfaces.Landmark
                        }
                        else -> {
                            throw IllegalStateException()
                        }
                    }
                }

        return@map resolvedLandmarks
    })

    override fun getLandmarks(): LiveData<List<org.taskforce.episample.core.interfaces.Landmark>> {
        return getLandmarks
    }

    val collectItemsMediator = LiveDataPair(studyRepository.getEnumerations(userSession.studyId), getLandmarks)
    override fun getCollectItems(): LiveData<List<CollectItem>> {
        return Transformations.map(collectItemsMediator, {
            return@map it.first.map { it.makeLiveEnumeration() } + it.second.map { it as CollectItem }
        })
    }

    override fun getBreadcrumbs(): LiveData<List<Breadcrumb>> {
        return Transformations.map(studyRepository.getBreadcrumbs(studyId), {
            return@map it.map { LiveBreadcrumb(it.location, it.gpsPrecision, it.dateCreated) }
        })
    }

    override fun getLandmarkTypes(): LiveData<List<org.taskforce.episample.core.interfaces.LandmarkType>> {
        return Transformations.map(configManager.getConfig(application), {
            return@map it.landmarkTypes
        })
    }

    override fun addBreadcrumb(breadcrumb: Breadcrumb, callback: (breadcrumbId: String) -> Unit) {
        studyRepository.addBreadcrumb(breadcrumb.toDBBreadcrumb(userSession.username, studyId), callback)
    }

    override fun updateEnumerationItem(item: org.taskforce.episample.core.interfaces.Enumeration, callback: () -> Unit) {
        studyRepository.updateEnumerationItem(item.toDBEnumeration(userSession.username, studyId), listOf(), callback)
    }

    override fun updateLandmark(landmark: org.taskforce.episample.core.interfaces.Landmark, callback: () -> Unit) {
        studyRepository.updateLandmark(landmark.toDBLandmark(userSession.username, studyId), callback)

    }

    override fun addEnumerationItem(item: org.taskforce.episample.core.interfaces.Enumeration, callback: (enumerationId: String) -> Unit) {
        studyRepository.insertEnumerationItem(item.toDBEnumeration(userSession.username, studyId), item.customFieldValues, callback)
    }

    override fun addLandmark(landmark: org.taskforce.episample.core.interfaces.Landmark, callback: (landmarkId: String) -> Unit) {
        studyRepository.insertLandmarkItem(landmark.toDBLandmark(userSession.username, studyId), callback)
    }
}