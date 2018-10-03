package org.taskforce.episample.db

import android.app.Application
import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import org.taskforce.episample.config.base.ConfigManagerException
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.landmark.CustomLandmarkType
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.RuleSet
import org.taskforce.episample.db.sampling.strata.Strata
import org.taskforce.episample.db.sampling.subsets.Subset
import org.taskforce.episample.utils.makeDBConfig

class ConfigRepository(application: Application, injectedDatabase: ConfigRoomDatabase? = null, injectedStudyDatabase: StudyRoomDatabase? = null) {

    lateinit var db: ConfigRoomDatabase
    private val studyRepository = StudyRepository(application, injectedStudyDatabase)

    fun cleanUp() {
        studyRepository.cleanUp()
    }

    init {
        injectedDatabase?.let {
            db = it
        } ?: run {
            db = ConfigRoomDatabase.getDatabase(application)
        }
    }

    private val configDao: ConfigDao = db.configDao()
    private val resolvedConfigDao: ResolvedConfigDao = db.resolvedConfigDao()

    private val allConfigs = configDao.getAllConfigs()
    private val availableConfigs: LiveData<List<Config>> = configDao.getAllConfigs()

    // Wrap Dao LiveData
    fun getAllConfigs(): LiveData<List<Config>> {
        return allConfigs
    }

    fun getAvailableConfigs(): LiveData<List<Config>> {
        return availableConfigs
    }

    fun getConfig(configId: String): LiveData<Config> {
        return configDao.getConfig(configId)
    }

    fun getResolvedConfig(configId: String): LiveData<ResolvedConfig> {
        return resolvedConfigDao.getConfig(configId)
    }

    fun getResolvedConfigSync(configId: String): ResolvedConfig {
        return resolvedConfigDao.getConfigSync(configId)
    }

    fun getConfigSync(configId: String): Config {
        return configDao.getConfigSync(configId)
    }

    // Domain Actions
    fun insertConfigFromBuildManager(config: org.taskforce.episample.config.base.Config, callback: (configId: String) -> Unit) {
        val insertConfig = Config(config.name, config.dateCreated, config.id)
        val insertAdminSettings = config.adminSettings?.let {
            AdminSettings(it.password, insertConfig.id)
        }
        val insertEnumerationSubject = config.enumerationSubject?.let {
            EnumerationSubject(it.singular, it.plural, it.primaryLabel, insertConfig.id)

        }

        val insertCustomFields = config.customFields.map {
            it.makeDBConfig(insertConfig.id)
        }

        val insertUserSettings = config.userSettings?.let {
            UserSettings(it.gpsMinimumPrecision, it.gpsPreferredPrecision, it.allowPhotos, it.photoCompressionScale, insertConfig.id)
        }

        val insertDisplaySettings = DisplaySettings(config.displaySettings.isDateMetric, config.displaySettings.isTime24Hour, insertConfig.id)

        val insertLandmarks = config.customLandmarkTypes.map {
            CustomLandmarkType(it.name, it.iconLocation, insertConfig.id)
        }

        val insertSubsets = config.subsets
        val insertStrata = config.strata
        val insertRules = config.rules
        val insertRuleSets = config.ruleSets

        val insertEnumerationAreaPoints = config.enumerationAreas.map { area ->
            area.points.map { (lat, long) ->
                EnumerationAreaPoint(lat, long, area.id)
            }
        }.flatMap {
            it
        }

        val insertEnumerationAreas = config.enumerationAreas.map {
            EnumerationArea(it.name, insertConfig.id, it.id)
        }

        val insertConfigInput = InsertConfigInput(
                insertConfig,
                insertAdminSettings,
                insertEnumerationSubject,
                insertCustomFields,
                insertLandmarks,
                insertUserSettings,
                insertDisplaySettings,
                insertStrata,
                insertSubsets,
                insertRuleSets,
                insertRules,
                insertEnumerationAreas,
                insertEnumerationAreaPoints,
                callback)
        
        InsertConfigAsyncTask(configDao).execute(insertConfigInput)
    }

    fun duplicateConfig(config: Config, callback: (configId: String) -> Unit) {
        var newName = config.name

        while (
                allConfigs.value?.map({ it.name })?.contains(newName) == true
                && newName.length <= org.taskforce.episample.config.base.Config.nameMaxChars
        ) {
            newName = "$newName-1"
        }

        if (newName.length > org.taskforce.episample.config.base.Config.nameMaxChars) {
            throw ConfigManagerException("Configuration cannot be duplicated because name is too long. Please edit the name and try again.")
        }

        DuplicateAsyncTask(configDao).execute(Triple(config, newName, callback))
    }

    fun deleteConfig(config: Config, callback: () -> Unit) {
        DeleteAsyncTask(configDao).execute(Pair(config, callback))
    }

    fun insertStudy(sourceConfig: ResolvedConfig, name: String, studyPassword: String, callback: (configId: String, studyId: String) -> Unit) {
        studyRepository.insertStudy(sourceConfig, name, studyPassword, callback)
    }
}


private data class InsertConfigInput(val config: Config,
                                     val adminSettings: AdminSettings?,
                                     val enumerationSubject: EnumerationSubject?,
                                     val customFields: List<CustomField>,
                                     val landmarks: List<CustomLandmarkType>,
                                     val userSettings: UserSettings?,
                                     val displaySettings: DisplaySettings,
                                     val insertStrata: List<Strata>,
                                     val insertSubsets: List<Subset>,
                                     val insertRuleSets: List<RuleSet>,
                                     val insertRules: List<RuleRecord>,
                                     val enumerationAreas: List<EnumerationArea>,
                                     val enumerationAreaPoints: List<EnumerationAreaPoint>,
                                     val callback: (configId: String) -> Unit)

private class InsertConfigAsyncTask(private val asyncTaskDao: ConfigDao) : AsyncTask<InsertConfigInput, Void, Void>() {

    override fun doInBackground(vararg params: InsertConfigInput): Void? {
        val input = params[0]
        asyncTaskDao.insert(
                input.config,
                input.customFields,
                input.landmarks,
                input.adminSettings,
                input.enumerationSubject,
                input.userSettings,
                input.displaySettings,
                input.insertStrata,
                input.insertSubsets,
                input.insertRuleSets,
                input.insertRules,
                input.enumerationAreas,
                input.enumerationAreaPoints
        )
        input.callback(input.config.id)
        return null
    }
}

private class DeleteAsyncTask(private val asyncTaskDao: ConfigDao) : AsyncTask<Pair<Config, () -> Unit>, Void, Void>() {
    override fun doInBackground(vararg params: Pair<Config, () -> Unit>): Void? {
        val config = params[0].first
        val callback = params[0].second
        asyncTaskDao.delete(config)
        callback()
        return null
    }
}

private class DuplicateAsyncTask(private val asyncTaskDao: ConfigDao) : AsyncTask<Triple<Config, String, (configId: String) -> Unit>, Void, Void>() {
    override fun doInBackground(vararg params: Triple<Config, String, (configId: String) -> Unit>): Void? {
        val originalConfig = params[0].first
        val newName = params[0].second
        val callback = params[0].third

        callback(asyncTaskDao.duplicate(originalConfig, newName))
        return null
    }
}