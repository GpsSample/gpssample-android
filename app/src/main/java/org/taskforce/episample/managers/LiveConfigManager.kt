package org.taskforce.episample.managers

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.content.Context
import org.taskforce.episample.config.sampling.ResolvedSamplingMethodEntity
import org.taskforce.episample.core.interfaces.GeoJsonEnumerationArea
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.config.ResolvedConfig
import org.taskforce.episample.core.BuiltInLandmark
import org.taskforce.episample.core.interfaces.*

class LiveConfigManager(val studyRepository: StudyRepository,
                        override val configId: String) : ConfigManager {

    override fun getConfig(context: Context): LiveData<Config> {
        return Transformations.map(studyRepository.getResolvedConfig(configId), {
            return@map org.taskforce.episample.managers.LiveConfig(context, it)
        })
    }
}

class LiveConfig(context: Context, val dbConfig: ResolvedConfig) : Config {
    override val methodology: ResolvedSamplingMethodEntity
        get() = dbConfig.methodology

    override var name: String = dbConfig.name
    override val dateCreated = dbConfig.dateCreated
    override val id = dbConfig.id
    override var adminSettings: AdminSettings = dbConfig.adminSettings
    override val userSettings: UserSettings = dbConfig.userSettings
    override val displaySettings: DisplaySettings = dbConfig.displaySettings
    override var enumerationSubject: EnumerationSubject = dbConfig.enumerationSubject
    override var customFields: List<CustomField> = dbConfig.customFields.map { it as CustomField }
    override val landmarkTypes: List<LandmarkType> = BuiltInLandmark.getLandmarkTypes(context) + dbConfig.customLandmarkTypes
    override val enumerationAreas: List<EnumerationArea> = dbConfig.enumerationAreas.map { resolvedEnumerationArea ->
        val points = resolvedEnumerationArea.points.map {
            Pair(it.lat, it.lng)
        }
        GeoJsonEnumerationArea(resolvedEnumerationArea.name,
                points,
                resolvedEnumerationArea.configId,
                resolvedEnumerationArea.id)
    }
    override val mapboxStyle = dbConfig.mapboxStyle
    override val mapMinZoom: Double = dbConfig.mapMinZoom
    override val mapMaxZoom: Double = dbConfig.mapMaxZoom
}