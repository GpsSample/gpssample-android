package org.taskforce.episample.db.config

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.db.DateRange
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.navigation.ResolvedNavigationPlan
import org.taskforce.episample.db.sampling.SampleEntity
import org.taskforce.episample.db.sampling.WarningEntity
import org.taskforce.episample.utils.toDBEnumeration
import org.taskforce.episample.utils.toDBLandmark
import kotlin.concurrent.thread

class CommonManager {
    companion object {
        fun getLandmarks(config: Config, studyRepository: StudyRepository, studyId: String): LiveData<List<Landmark>> {
            return Transformations.map(studyRepository.getLandmarks(studyId), { landmarks ->
                return@map landmarks.map { dbLandmark ->
                    when (dbLandmark.metadata) {
                        is LandmarkTypeMetadata.CustomId -> {
                            val customId = dbLandmark.metadata.id
                            val matchingType = config.landmarkTypes.filter {
                                (it.metadata as? LandmarkTypeMetadata.CustomId)?.id == customId
                            }.first()


                            return@map dbLandmark.makeLiveLandmark(matchingType) as Landmark
                        }
                        is LandmarkTypeMetadata.BuiltInLandmark -> {
                            val landmarkType = config.let {
                                return@let it.landmarkTypes.filter {
                                    (it.metadata as? LandmarkTypeMetadata.BuiltInLandmark)?.type == dbLandmark.metadata.type
                                }.first()
                            }
                            return@map dbLandmark.makeLiveLandmark(landmarkType) as Landmark
                        }
                        else -> {
                            throw IllegalStateException()
                        }
                    }
                }
            })
        }

        fun getCollectItems(config: Config, studyRepository: StudyRepository, studyId: String): LiveData<List<CollectItem>> {
            val collectItemsMediator = LiveDataPair(studyRepository.getEnumerations(studyId),
                    getLandmarks(config, studyRepository, studyId))

            return Transformations.map(collectItemsMediator, {
                return@map it.first.map { it.makeLiveEnumeration() } + it.second.map { it as CollectItem }
            })
        }

        fun getBreadcrumbs(studyRepository: StudyRepository, studyId: String): LiveData<List<Breadcrumb>> {
            return Transformations.map(studyRepository.getBreadcrumbs(studyId), {
                return@map it.map { LiveBreadcrumb(it.collectorName, it.location, it.gpsPrecision, it.startOfSession, it.dateCreated) }
            })
        }
    }
}

class LiveCollectManager(val application: Application,
                         val configManager: ConfigManager,
                         val config: Config,
                         val studyRepository: StudyRepository,
                         override val userSession: UserSession) : CollectManager {
    

    val studyId: String
        get() = userSession.studyId
    val configId: String
        get() = userSession.configId

    override fun getNumberOfNavigationPlans(): LiveData<Int> = studyRepository.getNumberOfNavigationPlans(studyId)

    override fun getNavigationPlans(): LiveData<List<ResolvedNavigationPlan>> = studyRepository.getNavigationPlans()

    override fun deleteSamples() {
        thread {
            studyRepository.deleteNavigationPlans()
            studyRepository.deleteSamples()
        }
    }

    override fun createNavigationPlans(sampleEntity: SampleEntity, numberOfNavigationPlansToMake: Int) {
        thread {
            studyRepository.createNavigationPlans(sampleEntity, numberOfNavigationPlansToMake)
        }
    }

    override fun getNumberOfEnumerationsInSample(): LiveData<Int> = studyRepository.getNumberOfEnumerationsInSample(studyId)

    override fun getSample(): LiveData<SampleEntity?> = studyRepository.getSample(studyId)

    override fun getWarnings(): LiveData<List<WarningEntity>> = studyRepository.getWarnings(studyId)

    override fun getEnumerations(): LiveData<List<org.taskforce.episample.core.interfaces.Enumeration>> {
        return Transformations.map(studyRepository.getEnumerations(studyId), {
            return@map it?.map { it.makeLiveEnumeration() }
        })
    }

    override fun getNumberOfSamples(): LiveData<Int> {
        return studyRepository.getNumberOfSamples(studyId)
    }

    override fun getNumberOfValidEnumerations(): LiveData<Int> {
        return studyRepository.getNumberOfValidEnumerations(studyId)
    }

    override fun getValidEnumerationsDateRange(): LiveData<DateRange> {
        return studyRepository.getValidEnumerationsDateRange(studyId)
    }

    override fun getLandmarks(): LiveData<List<org.taskforce.episample.core.interfaces.Landmark>> {
        return CommonManager.getLandmarks(config, studyRepository, studyId)
    }

    override fun getCollectItems(): LiveData<List<CollectItem>> {
        return CommonManager.getCollectItems(config, studyRepository, studyId)
    }

    override fun deleteCollectItem(collectItem: CollectItem) {
        studyRepository.deleteCollectItem(collectItem, studyId)
    }

    override fun getLandmarkTypes(): List<org.taskforce.episample.core.interfaces.LandmarkType> {
        return config.landmarkTypes
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

    override fun createSample() {
        studyRepository.createSample(studyId, config, application.resources)
    }
}