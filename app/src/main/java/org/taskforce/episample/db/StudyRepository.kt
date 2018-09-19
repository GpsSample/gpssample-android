package org.taskforce.episample.db

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.os.AsyncTask
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreadcrumb
import org.taskforce.episample.db.collect.Landmark
import org.taskforce.episample.db.collect.ResolvedEnumeration
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.landmark.CustomLandmarkType
import java.util.*

class StudyRepository(application: Application, injectedDatabase: StudyRoomDatabase? = null) {
    lateinit var db: StudyRoomDatabase

    init {
        injectedDatabase?.let {
            db = it
        } ?: run {
            db = StudyRoomDatabase.getDatabase(application)
        }
    }

    private val configDao: ConfigDao = db.configDao()
    private val studyDao: StudyDao = db.studyDao()
    private val resolvedConfigDao: ResolvedConfigDao = db.resolvedConfigDao()

    private val allConfigs = configDao.getAllConfigs()

    private fun studyToNullable(studyId: String): LiveData<Study?> = Transformations.map(studyDao.getStudy(studyId)) {
        return@map it
    }

    private val study: LiveData<Study?> = Transformations.map(studyDao.getAllStudies(), {
        return@map it.firstOrNull()
    })

    // Wrap Dao LiveData
    fun getAllConfigs(): LiveData<List<Config>> {
        return allConfigs
    }

    fun getStudy(): LiveData<Study?> {
        return study
    }

    fun getAllStudiesSync(): List<Study> {
        return studyDao.getAllStudiesSync()
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

    fun getResolvedConfigSync(configId: String): ResolvedConfig {
        return resolvedConfigDao.getConfigSync(configId)
    }

    fun getConfigSync(configId: String): Config {
        return configDao.getConfigSync(configId)
    }

    // Domain Actions

    fun insertStudy(sourceConfig: ResolvedConfig, name: String, studyPassword: String, callback: (configId: String, studyId: String) -> Unit) {
        InsertStudyAsyncTask(studyDao).execute(InsertStudyInput(name, studyPassword, sourceConfig, callback))
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

    fun getAllStudies(): LiveData<List<Study>> {
        return studyDao.getAllStudies()
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


private data class InsertStudyInput(val name: String,
                                    val password: String,
                                    val sourceConfig: ResolvedConfig,
                                    val callback: (configId: String, studyId: String) -> Unit)

private class InsertStudyAsyncTask(private val studyDao: StudyDao) : AsyncTask<InsertStudyInput, Void, Void>() {
    override fun doInBackground(vararg params: InsertStudyInput): Void? {
        val name = params[0].name
        val password = params[0].password
        val sourceConfig = params[0].sourceConfig
        val callback = params[0].callback

        val studyId = UUID.randomUUID().toString()
        val configId = studyDao.insert(studyId, name, password, sourceConfig)
        callback(configId, studyId)
        return null
    }
}