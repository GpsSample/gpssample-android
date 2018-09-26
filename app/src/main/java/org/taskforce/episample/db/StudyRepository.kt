package org.taskforce.episample.db

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.os.AsyncTask
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.taskforce.episample.core.InitializedLiveData
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreadcrumb
import org.taskforce.episample.db.collect.Landmark
import org.taskforce.episample.db.collect.ResolvedEnumeration
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.navigation.NavigationDao
import org.taskforce.episample.db.navigation.ResolvedNavigationPlan
import org.taskforce.episample.sync.core.StudyDatabaseFilesChangedMessage
import java.util.*

class StudyRepository(val application: Application, injectedDatabase: StudyRoomDatabase? = null) {

    init {
        EventBus.getDefault().register(this)
    }

    fun cleanUp() {
        EventBus.getDefault().unregister(this)
    }

    private val studyDb = InitializedLiveData(injectedDatabase
            ?: StudyRoomDatabase.getDatabase(application))

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDatabaseFilesChanged(event: StudyDatabaseFilesChangedMessage) {
        studyDb.postValue(StudyRoomDatabase.reloadDatabaseInstance(application))
    }

    private val configDao: LiveData<ConfigDao> = Transformations.map(studyDb) {
        it.configDao()
    }

    /**
     * Make sure to initialize any Daos that need to write to the database
     * They are potentially accessed before being observed
     */
    private val studyDao: LiveData<StudyDao> = (Transformations.map(studyDb) {
        it.studyDao()
    } as MutableLiveData).apply {
        val defaultDatabase = injectedDatabase ?: StudyRoomDatabase.getDatabase(application)
        value = defaultDatabase.studyDao()
    }

    private val resolvedConfigDao: LiveData<ResolvedConfigDao> = Transformations.map(studyDb) {
        it.resolvedConfigDao()
    }
    private val navigationDao: LiveData<NavigationDao> = (Transformations.map(studyDb) {
        it.navigationDao()
    } as MutableLiveData).apply {
        val defaultDatabase = injectedDatabase ?: StudyRoomDatabase.getDatabase(application)
        value = defaultDatabase.navigationDao()
    }

    private val allConfigs = Transformations.switchMap(configDao, {
        it.getAllConfigs()
    })

    private val allStudies = Transformations.switchMap(studyDao) {
        it.getAllStudies()
    }

    private val study: LiveData<Study?> = Transformations.map(allStudies, {
        return@map it.firstOrNull()
    })

    // Wrap Dao LiveData
    fun getAllConfigs(): LiveData<List<Config>> {
        return allConfigs
    }

    fun getStudy(): LiveData<Study?> {
        return study
    }

    fun getConfig(configId: String): LiveData<Config> {
        return Transformations.switchMap(configDao) {
            it.getConfig(configId)
        }
    }

    fun getResolvedConfig(configId: String): LiveData<ResolvedConfig> {
        return Transformations.switchMap(resolvedConfigDao) {
            it.getConfig(configId)
        }
    }

    fun getEnumerations(studyId: String): LiveData<List<ResolvedEnumeration>> {
        return Transformations.switchMap(studyDao) {
            it.getEnumerations(studyId)
        }
    }

    fun getResolvedConfigSync(configId: String): ResolvedConfig {
        return studyDb.value!!.resolvedConfigDao().getConfigSync(configId)
    }

    fun getConfigSync(configId: String): Config {
        return studyDb.value!!.configDao().getConfigSync(configId)
    }

    // Domain Actions

    fun insertStudy(sourceConfig: ResolvedConfig, name: String, studyPassword: String, callback: (configId: String, studyId: String) -> Unit) {
        studyDao.value?.let { studyDao ->
            InsertStudyAsyncTask(studyDao).execute(InsertStudyInput(name, studyPassword, sourceConfig, callback))
        }
    }

    fun insertEnumerationItem(item: Enumeration, customFieldValues: List<org.taskforce.episample.core.interfaces.CustomFieldValue>, callback: (enumerationId: String) -> Unit) {
        val dbValues = customFieldValues.map {
            return@map CustomFieldValue.makeDBCustomFieldValue(it, item.id)
        }

        studyDao.value?.let { studyDao ->
            InsertEnumerationTask(studyDao).execute(Triple(item, dbValues, callback))
        }
    }

    fun updateEnumerationItem(item: Enumeration, customFieldValues: List<CustomFieldValue>, callback: () -> Unit) {
        studyDao.value?.let { studyDao ->
            UpdateEnumerationTask(studyDao).execute(Triple(item, customFieldValues, callback))
        }
    }

    fun insertLandmarkItem(item: Landmark, callback: (landmarkId: String) -> Unit) {
        studyDao.value?.let { studyDao ->
            InsertLandmarkTask(studyDao).execute(Pair(item, callback))
        }
    }

    fun updateLandmark(landmark: Landmark, callback: () -> Unit) {
        studyDao.value?.let { studyDao ->
            UpdateLandmarkTask(studyDao).execute(Pair(landmark, callback))
        }
    }

    fun addBreadcrumb(breadcrumb: GpsBreadcrumb, callback: (breadcrumbId: String) -> Unit) {
        studyDao.value?.let { studyDao ->
            InsertBreadcrumbTask(studyDao).execute(Pair(breadcrumb, callback))
        }
    }

    fun updateNavigationItem(navigationItemId: String, surveyStatus: SurveyStatus, callback: () -> Unit) {
        navigationDao.value?.let { navigationDao ->
            UpdateNavigationItemTask(navigationDao).execute(Triple(navigationItemId, surveyStatus, callback))
        }
    }

    fun getResolvedEnumerationsSync(studyId: String): List<ResolvedEnumeration> {
        return studyDao.value!!.getResolvedEnumerationsSync(studyId)
    }

    fun getLandmarks(studyId: String): LiveData<List<Landmark>> {
        return Transformations.switchMap(studyDao) {
            it.getLandmarks(studyId)
        }
    }

    fun getBreadcrumbs(studyId: String): LiveData<List<GpsBreadcrumb>> {
        return Transformations.switchMap(studyDao) {
            it.getBreadcrumbs(studyId)
        }
    }

    fun getAllStudies(): LiveData<List<Study>> {
        return Transformations.switchMap(studyDao) {
            it.getAllStudies()
        }
    }

    fun getNavigationPlan(navigationPlanId: String): LiveData<ResolvedNavigationPlan> {
        return Transformations.switchMap(navigationDao) {
            it.getNavigationPlan(navigationPlanId)
        }
    }

    fun getNavigationPlans(): LiveData<List<ResolvedNavigationPlan>> {
        return Transformations.switchMap(navigationDao) {
            it.getAllNavigationPlans()
        }
    }

    fun createDemoNavigationPlan(studyId: String, callback: (navigationPlanId: String) -> Unit) {
        navigationDao.value?.let { navigationDao ->
            studyDao.value?.let { studyDao ->
                InsertDemoNavigationPlanTask(navigationDao, studyDao).execute(Pair(studyId, callback))
            }
        }
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

private class UpdateNavigationItemTask(private val navigationDao: NavigationDao) : AsyncTask<Triple<String, SurveyStatus, () -> Unit>, Void, Void>() {
    override fun doInBackground(vararg params: Triple<String, SurveyStatus, () -> Unit>): Void? {
        val navigationItemId = params[0].first
        val surveyStatus = params[0].second
        val callback = params[0].third

        navigationDao.updateNavigationItem(navigationItemId, surveyStatus)
        callback()
        return null
    }
}

private class InsertDemoNavigationPlanTask(private val navigationDao: NavigationDao, private val studyDao: StudyDao) : AsyncTask<Pair<String, (navigationPlanId: String) -> Unit>, Void, Void>() {
    override fun doInBackground(vararg params: Pair<String, (navigationPlanId: String) -> Unit>): Void? {
        val studyId = params[0].first
        val callback = params[0].second

        val enumerations = studyDao.getResolvedEnumerationsSync(studyId)

        callback(navigationDao.createDemoNavigationPlan(studyId, enumerations))

        return null
    }
}
