package org.taskforce.episample.db

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.os.AsyncTask
import org.taskforce.episample.config.base.ConfigManagerException
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreadcrumb
import org.taskforce.episample.db.collect.Landmark
import org.taskforce.episample.db.collect.ResolvedEnumeration
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.landmark.CustomLandmarkType
import org.taskforce.episample.utils.makeDBConfig
import java.util.*

class ConfigRepository(application: Application, injectedDatabase: ConfigRoomDatabase? = null) {

    // TODO setup repository to inject database (exposing here for testing)
    lateinit var db: ConfigRoomDatabase

    init {
        injectedDatabase?.let {
            db = it
        } ?: run {
            db = ConfigRoomDatabase.getDatabase(application)
        }
    }

    private val configDao: ConfigDao = db.configDao()
    private val studyDao: StudyDao = db.studyDao()
    private val resolvedConfigDao: ResolvedConfigDao = db.resolvedConfigDao()

    private val allConfigs = configDao.getAllConfigs()
    private val availableConfigs: LiveData<List<Config>> = Transformations.map(allConfigs, {
        return@map it?.filter { config ->
            config.studyId == null
        }
    })

    private fun studyToNullable(studyId: String): LiveData<Study?> = Transformations.map(studyDao.getStudy(studyId)) {
        return@map it
    }

    private val study: LiveData<Study?> = Transformations.switchMap(allConfigs, {
        val studyConfig = it.firstOrNull() { config ->
            config.studyId != null
        }

        studyConfig?.let {
            return@switchMap studyToNullable(it.studyId!!)
        }
        return@switchMap MutableLiveData<Study?>().apply { value = null }
    })

    // Wrap Dao LiveData
    fun getAllConfigs(): LiveData<List<Config>> {
        return allConfigs
    }

    fun getAvailableConfigs(): LiveData<List<Config>> {
        return availableConfigs
    }

    fun getStudy(): LiveData<Study?> {
        return study
    }

    fun getConfig(configId: String): LiveData<Config> {
        return configDao.getConfig(configId)
    }

    fun getResolvedConfig(configId: String): LiveData<ResolvedConfig> {
        return resolvedConfigDao.getConfig(configId)
    }

    fun getEnumerations(studyId: String): LiveData<List<ResolvedEnumeration>> {
        return studyDao.getEnumerations(studyId)
    }

    fun getEnumerationsSync(studyId: String): List<Enumeration> {
        return studyDao.getEnumerationsSync(studyId)
    }

    fun getResolvedConfigSync(configId: String): List<ResolvedConfig> {
        return resolvedConfigDao.getConfigSync(configId)
    }

    fun getConfigSync(configId: String): Config {
        return configDao.getConfigSync(configId)
    }

    // Domain Actions
    fun insertConfigFromBuildManager(config: org.taskforce.episample.config.base.Config, callback: (configId: String) -> Unit) {
        val insertConfig = Config(config.name, config.dateCreated)
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
            UserSettings(it.gpsMinimumPrecision, it.gpsPreferredPrecision, it.allowPhotos, insertConfig.id)
        }

        val insertDisplaySettings = DisplaySettings(config.displaySettings.isDateMetric, config.displaySettings.isTime24Hour, insertConfig.id)

        val insertLandmarks = config.customLandmarkTypes.map {
            CustomLandmarkType(it.name, it.iconLocation, insertConfig.id)
        }

        InsertConfigAsyncTask(configDao).execute(InsertConfigInput(insertConfig, insertAdminSettings, insertEnumerationSubject, insertCustomFields, insertLandmarks, insertUserSettings, insertDisplaySettings, callback))
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

    fun insertStudy(sourceConfig: Config, name: String, studyPassword: String, callback: (configId: String, studyId: String) -> Unit) {
        val insertStudy = Study(name, studyPassword, Date())
        InsertStudyAsyncTask(studyDao).execute(InsertStudyInput(insertStudy, sourceConfig.id, callback))
    }

    fun deleteConfig(config: Config, callback: () -> Unit) {
        DeleteAsyncTask(configDao).execute(Pair(config, callback))
    }

    fun insertEnumerationItem(item: Enumeration, customFieldValues: List<org.taskforce.episample.core.interfaces.CustomFieldValue>, callback: (enumerationId: String) -> Unit) {
        val dbValues = customFieldValues.map {
            return@map CustomFieldValue.makeDBCustomFieldValue(it, item.id)
        }
        InsertEnumerationTask(studyDao).execute(Triple(item, dbValues, callback))
    }

    fun updateEnumerationItem(item: Enumeration, customFieldValues: List<CustomFieldValue>, callback: () -> Unit) {
        UpdateEnumerationTask(studyDao).execute(Triple(item, customFieldValues, callback))
    }

    fun insertLandmarkItem(item: Landmark, callback: (landmarkId: String) -> Unit) {
        InsertLandmarkTask(studyDao).execute(Pair(item, callback))
    }

    fun updateLandmark(landmark: Landmark, callback: () -> Unit) {
        UpdateLandmarkTask(studyDao).execute(Pair(landmark, callback))
    }

    fun addBreadcrumb(breadcrumb: GpsBreadcrumb, callback: (breadcrumbId: String) -> Unit) {
        InsertBreadcrumbTask(studyDao).execute(Pair(breadcrumb, callback))
    }

    fun getResolvedEnumerationsSync(studyId: String): List<ResolvedEnumeration> {
        return studyDao.getResolvedEnumerationsSync(studyId)
    }

    fun getLandmarks(studyId: String): LiveData<List<Landmark>> {
        return studyDao.getLandmarks(studyId)
    }

    fun getBreadcrumbs(studyId: String): LiveData<List<GpsBreadcrumb>> {
        return studyDao.getBreadcrumbs(studyId)
    }

    fun getCustomLandmarkTypes(configId: String): LiveData<List<CustomLandmarkType>> {
        return studyDao.getCustomLandmarkTypes(configId)
    }
}


private data class InsertConfigInput(val config: Config,
                                     val adminSettings: AdminSettings?,
                                     val enumerationSubject: EnumerationSubject?,
                                     val customFields: List<CustomField>,
                                     val landmarks: List<CustomLandmarkType>,
                                     val userSettings: UserSettings?,
                                     val displaySettings: DisplaySettings,
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
                input.displaySettings
        )
        input.callback(input.config.id)
        return null
    }
}

private class InsertEnumerationTask(private val studyDao: StudyDao) : AsyncTask<Triple<Enumeration, List<CustomFieldValue>, (enumerationId: String) -> Unit>, Void, Void>() {
    override fun doInBackground(vararg params: Triple<Enumeration, List<CustomFieldValue>, (enumerationId: String) -> Unit>): Void? {
        val enumerationItem = params[0].first
        val customFieldValues = params[0].second
        val callback = params[0].third
        studyDao.insert(enumerationItem, customFieldValues)
        callback(enumerationItem.id)
        return null
    }
}

private class UpdateEnumerationTask(private val studyDao: StudyDao) : AsyncTask<Triple<Enumeration, List<CustomFieldValue>, () -> Unit>, Void, Void>() {
    override fun doInBackground(vararg params: Triple<Enumeration, List<CustomFieldValue>, () -> Unit>): Void? {
        val enumerationItem = params[0].first
        val customFieldValues = params[0].second
        val callback = params[0].third
        studyDao.update(enumerationItem, customFieldValues)
        callback()
        return null
    }
}

private class InsertLandmarkTask(private val studyDao: StudyDao) : AsyncTask<Pair<Landmark, (landmarkId: String) -> Unit>, Void, Void>() {
    override fun doInBackground(vararg params: Pair<Landmark, (landmarkId: String) -> Unit>): Void? {
        val landmark = params[0].first
        val callback = params[0].second
        studyDao.insert(landmark)
        callback(landmark.id)
        return null
    }
}

private class UpdateLandmarkTask(private val studyDao: StudyDao) : AsyncTask<Pair<Landmark, () -> Unit>, Void, Void>() {
    override fun doInBackground(vararg params: Pair<Landmark, () -> Unit>): Void? {
        val landmark = params[0].first
        val callback = params[0].second
        studyDao.update(landmark)
        callback()
        return null
    }
}

private class InsertBreadcrumbTask(private val studyDao: StudyDao) : AsyncTask<Pair<GpsBreadcrumb, (breadcrumbId: String) -> Unit>, Void, Void>() {
    override fun doInBackground(vararg params: Pair<GpsBreadcrumb, (breadcrumbId: String) -> Unit>): Void? {
        val breadcrumb = params[0].first
        val callback = params[0].second
        studyDao.insert(breadcrumb)
        callback(breadcrumb.id)
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

private data class InsertStudyInput(val study: Study,
                                    val sourceConfigId: String,
                                    val callback: (configId: String, studyId: String) -> Unit)

private class InsertStudyAsyncTask(private val studyDao: StudyDao) : AsyncTask<InsertStudyInput, Void, Void>() {
    override fun doInBackground(vararg params: InsertStudyInput): Void? {
        val study = params[0].study
        val sourceConfigId = params[0].sourceConfigId
        val callback = params[0].callback

        val configId = studyDao.insert(study, sourceConfigId)
        callback(configId, study.id)
        return null
    }
}