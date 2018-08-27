package org.taskforce.episample.db

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.os.AsyncTask
import org.taskforce.episample.config.base.ConfigManagerException
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomField
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

    fun getEnumerations(configId: String): LiveData<List<Enumeration>> {
        return studyDao.getEnumerations(configId)
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
            EnumerationSubject(it, "$it + PLURAL", "$it + LABEL", insertConfig.id)
        }

        val insertCustomFields = config.customFields.map {
            it.makeDBConfig(insertConfig.id)
        }

        val insertUserSettings = config.userSettings?.let {
            UserSettings(it.gpsMinimumPrecision, it.gpsPreferredPrecision, insertConfig.id)
        }

        InsertConfigAsyncTask(configDao).execute(InsertConfigInput(insertConfig, insertAdminSettings, insertEnumerationSubject, insertCustomFields, insertUserSettings, callback))
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

    fun insertStudy(config: Config, name: String, studyPassword: String, callback: (configId: String, studyId: String) -> Unit) {
        val insertStudy = Study(name, studyPassword, Date())
        val insertConfig = Config(config.name, studyId = insertStudy.id)
        InsertStudyAsyncTask(studyDao).execute(InsertStudyInput(insertConfig, insertStudy, config.id, callback))
    }

    fun deleteConfig(config: Config, callback: () -> Unit) {
        DeleteAsyncTask(configDao).execute(Pair(config, callback))
    }
}


private data class InsertConfigInput(val config: Config,
                                     val adminSettings: AdminSettings?,
                                     val enumerationSubject: EnumerationSubject?,
                                     val customFields: List<CustomField>,
                                     val userSettings: UserSettings?,
                                     val callback: (configId: String) -> Unit)

private class InsertConfigAsyncTask(private val asyncTaskDao: ConfigDao) : AsyncTask<InsertConfigInput, Void, Void>() {

    override fun doInBackground(vararg params: InsertConfigInput?): Void? {
        val input = params[0]!!
        asyncTaskDao.insert(
                input.config,
                input.customFields,
                input.adminSettings,
                input.enumerationSubject,
                input.userSettings
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

private data class InsertStudyInput(val insertConfig: Config,
                                    val study: Study,
                                    val sourceConfigId: String,
                                    val callback: (configId: String, studyId: String) -> Unit)

private class InsertStudyAsyncTask(private val studyDao: StudyDao) : AsyncTask<InsertStudyInput, Void, Void>() {
    override fun doInBackground(vararg params: InsertStudyInput): Void? {
        val insertConfig = params[0].insertConfig
        val study = params[0].study
        val sourceConfigId = params[0].sourceConfigId
        val callback = params[0].callback

        studyDao.insert(study, insertConfig, sourceConfigId)
        callback(insertConfig.id, study.id)
        return null
    }
}