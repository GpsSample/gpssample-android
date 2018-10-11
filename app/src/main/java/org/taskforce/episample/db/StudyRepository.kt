package org.taskforce.episample.db

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.content.res.Resources
import android.os.AsyncTask
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.taskforce.episample.R
import org.taskforce.episample.config.sampling.SamplingMethod
import org.taskforce.episample.config.sampling.SamplingMethodology
import org.taskforce.episample.config.sampling.SamplingUnits
import org.taskforce.episample.core.InitializedLiveData
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreadcrumb
import org.taskforce.episample.db.collect.Landmark
import org.taskforce.episample.db.collect.ResolvedEnumeration
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.filter.ResolvedRuleSet
import org.taskforce.episample.db.navigation.NavigationDao
import org.taskforce.episample.db.navigation.ResolvedNavigationPlan
import org.taskforce.episample.db.sampling.SampleEntity
import org.taskforce.episample.db.sampling.WarningEntity
import org.taskforce.episample.sync.core.EnumerationsReceivedMessage
import org.taskforce.episample.sync.core.StudyReceivedMessage
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
    fun onDatabaseFilesChanged(event: StudyReceivedMessage) {
        studyDb.postValue(StudyRoomDatabase.reloadDatabaseInstance(application))
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onSyncUpdatesReceived(event: EnumerationsReceivedMessage) {
        val targetDatabase = StudyRoomDatabase.getDatabase(application)
        val sourceDatabase = StudyRoomDatabase.reloadIncomingInstance(application)

        val sourceDao = sourceDatabase.transferDao()
        val targetDao = targetDatabase.transferDao()

        targetDao.transfer(
                sourceDao.getEnumerations(),
                sourceDao.getLandmarks(),
                sourceDao.getBreadcrumbs(),
                sourceDao.getCustomFieldValues(),
                sourceDao.getNavigationPlans(),
                sourceDao.getNavigationItems(),
                sourceDao.getSamples(),
                sourceDao.getSampleEnumerations(),
                sourceDao.getSampleWarnings()
        )
    }

    /**
     * Make sure to initialize Daos
     * They are potentially accessed before being observed
     */
    private val studyDao: LiveData<StudyDao> = (Transformations.map(studyDb) {
        it.studyDao()
    } as MutableLiveData).apply {
        val defaultDatabase = injectedDatabase ?: StudyRoomDatabase.getDatabase(application)
        value = defaultDatabase.studyDao()
    }

    private val configDao: LiveData<ConfigDao> = (Transformations.map(studyDb) {
        it.configDao()
    } as MutableLiveData).apply {
        val defaultDatabase = injectedDatabase ?: StudyRoomDatabase.getDatabase(application)
        value = defaultDatabase.configDao()
    }

    private val resolvedConfigDao: LiveData<ResolvedConfigDao> = (Transformations.map(studyDb) {
        it.resolvedConfigDao()
    } as MutableLiveData).apply {
        val defaultDatabase = injectedDatabase ?: StudyRoomDatabase.getDatabase(application)
        value = defaultDatabase.resolvedConfigDao()
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

    fun getNumberOfValidEnumerations(studyId: String): LiveData<Int> {
        return Transformations.switchMap(studyDao) {
            it.getNumberOfValidEnumerations(studyId)
        }
    }

    fun getValidEnumerationsDateRange(studyId: String): LiveData<DateRange> {
        return Transformations.switchMap(studyDao) {
            it.getValidEnumerationsCollectionRange(studyId)
        }
    }

    fun createSample(studyId: String, config: org.taskforce.episample.core.interfaces.Config, resources: Resources) {
        studyDao.value?.let { CreateSampleTask(it, resources).execute(Triple(config, studyId, { _ -> })) }
    }

    fun getNumberOfSamples(studyId: String): LiveData<Int> = studyDao.value!!.getNumberOfSamples(studyId)
    fun getWarnings(studyId: String): LiveData<List<WarningEntity>> = studyDao.value!!.getWarnings(studyId)
    fun getSample(studyId: String): LiveData<SampleEntity> = studyDao.value!!.getSample(studyId)
    fun getNumberOfEnumerationsInSample(studyId: String): LiveData<Int>  = studyDao.value!!.getNumberOfEnumerationsInSample(studyId)
    fun deleteSamples() = studyDao.value!!.deleteSamples()
}

typealias SampleCreatedCallback = (Boolean) -> Unit

private class CreateSampleTask(private val studyDao: StudyDao, val resources: Resources) : AsyncTask<Triple<org.taskforce.episample.core.interfaces.Config, String, SampleCreatedCallback>, Void, Void>() {
    override fun doInBackground(vararg params: Triple<org.taskforce.episample.core.interfaces.Config, String, SampleCreatedCallback>): Void? {
        params[0].let { (config, studyId, callback) ->
            val samplingMethodology = config.methodology.toMethodology()
            val ruleSets = config.methodology.ruleSets
            val enumerations = studyDao.getValidEnumerationsSync(studyId)
            val enumerationsForRuleSets: List<Pair<ResolvedRuleSet, List<ResolvedEnumeration>>> = ruleSets.map { ruleSet ->
                val filteredEnumerationsForRuleSet = when(ruleSet.isAny) {
                    true -> ruleSet.filter.filterAny(enumerations)
                    false -> ruleSet.filter.filterAll(enumerations)
                }
                ruleSet to filteredEnumerationsForRuleSet
            }
            val warnings = mutableListOf<String>()
            val totalPopulationForSampling: List<ResolvedEnumeration> = enumerationsForRuleSets.map { (ruleSet, filteredEnumerationsForRuleSet) ->
                val amount: Int = when (samplingMethodology.units) {
                    SamplingUnits.PERCENT -> (ruleSet.sampleSize/100.0 * filteredEnumerationsForRuleSet.size).toInt()
                    SamplingUnits.FIXED -> ruleSet.sampleSize
                }
                val sample = when (samplingMethodology.type) {
                    SamplingMethodology.SIMPLE_RANDOM_SAMPLE -> SamplingMethod.simpleRandomSample(amount, filteredEnumerationsForRuleSet)
                    SamplingMethodology.SYSTEMATIC_RANDOM_SAMPLE -> SamplingMethod.systematicRandomSample(amount, filteredEnumerationsForRuleSet)
                }
                if (sample.size < amount) {
                    warnings.add(resources.getString(R.string.insufficient_households, ruleSet.name, sample.size.toString(), amount.toString()))
                }
                return@map ruleSet to sample
            }.flatMap { it.second }
            //SAMPLING DONE -- move to insert data
            val sampleEntity = SampleEntity(studyId)
            val sampleEnumerations = totalPopulationForSampling.map { it.toSampleEnumerationEntity(sampleEntity.id) }
            val sampleWarnings = warnings.map { WarningEntity(sampleEntity.id, it) }
            studyDao.deleteSamples()
            studyDao.insert(sampleEntity)
            studyDao.insert(*sampleEnumerations.toTypedArray())
            studyDao.insert(*sampleWarnings.toTypedArray())
            callback(true)
            return null
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

        val enumerations = studyDao.getResolvedEnumerationsSync(studyId).shuffled()

        callback(navigationDao.createDemoNavigationPlan(studyId, enumerations))

        return null
    }
}
