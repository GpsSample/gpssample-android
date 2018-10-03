package org.taskforce.episample.config.base

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.taskforce.episample.R
import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.config.fields.CustomFieldDefaultProvider
import org.taskforce.episample.config.fields.CustomFieldTypeConstants
import org.taskforce.episample.config.language.CustomLanguage
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.sampling.SamplingMethod
import org.taskforce.episample.config.sampling.SamplingMethodChanged
import org.taskforce.episample.config.sampling.strata.StrataUpdated
import org.taskforce.episample.config.sampling.subsets.SubsetsUpdated
import org.taskforce.episample.config.settings.admin.AdminSettings
import org.taskforce.episample.config.settings.server.ServerSettings
import org.taskforce.episample.config.settings.user.UserSettings
import org.taskforce.episample.core.interfaces.EnumerationArea
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.RuleSet
import org.taskforce.episample.db.sampling.strata.Strata
import org.taskforce.episample.db.sampling.subsets.Subset
import org.taskforce.episample.fileImport.models.LandmarkType
import java.io.Serializable
import java.util.*

class ConfigBuildViewModel: ViewModel() {
    val configBuildManager = ConfigBuildManager()

    init {
        configBuildManager.eventBus.register(configBuildManager)
    }

    override fun onCleared() {
        super.onCleared()
        configBuildManager.eventBus.removeAllStickyEvents()
        configBuildManager.eventBus.unregister(configBuildManager)
    }
}

class ConfigBuildManager(val config: Config = Config(Date())):
        CustomFieldDefaultProvider, Serializable {

    val eventBus: EventBus = EventBus.getDefault()

    private var configCompletenessSubject = BehaviorSubject.create<Int>()

    private var customFieldSubject = BehaviorSubject.create<List<CustomField>>()

    val configCompletenessObservable: Observable<Int>
        get() = configCompletenessSubject

    val customFieldObservable: Observable<List<CustomField>>
        get() = customFieldSubject as Observable<List<CustomField>>

    init {
        configCompletenessSubject.onNext(config.completeness)
    }

    fun setName(name: String) {
        config.name = name
    }

    var languages: List<CustomLanguage>
        set(value) {
            config.customLanguages = value
        }
        get() = config.customLanguages

    fun setServerSettings(serverSettings: ServerSettings) {
        config.serverSettings = serverSettings
    }

    fun setAdminSettings(adminSettings: AdminSettings) {
        config.adminSettings = adminSettings
    }

    fun setFieldExportSettings(exportSettings: List<CustomField>) {
        config.customFields = exportSettings
    }

    fun setSamplingMethod(samplingMethod: SamplingMethod) {
        config.samplingMethod = samplingMethod
    }

    fun addCustomField(customField: CustomField) {
        val fields = config.customFields.toMutableList()
        fields.add(customField)
        config.customFields = fields.toList()
        customFieldSubject.onNext(config.customFields)
    }

    fun removeCustomField(customField: CustomField) {
        val fields = config.customFields.toMutableList()
        fields.remove(customField)
        config.customFields = fields.toList()
        customFieldSubject.onNext(config.customFields)
    }

    fun setUserSettings(userSettings: UserSettings) {
        config.userSettings = userSettings
    }

    fun setEnumerationSubject(enumerationSubject: EnumerationSubject) {
        config.enumerationSubject = enumerationSubject
    }

    fun setLandmarkTypes(landmarkTypes: List<LandmarkType>) {
        config.landmarkTypes = landmarkTypes
    }

    override fun defaultCustomFields(languageService: LanguageService): List<CustomField> = listOf(
                CustomField(
                        true,
                        false,
                        true,
                        true,
                        false,
                        languageService.getString(R.string.custom_field_record_id),
                        CustomFieldType.NUMBER,
                        mapOf(CustomFieldTypeConstants.INTEGER_ONLY to true)
                )
        )

    fun addRuleSet(ruleSet: RuleSet) {
        val mutableRuleSetList = config.ruleSets.toMutableList()
        mutableRuleSetList.add(ruleSet)
        config.ruleSets = mutableRuleSetList.toList()
    }

    fun addRules(rules: List<RuleRecord>) {
        val mutableRuleList = config.rules.toMutableList()
        mutableRuleList.addAll(rules)
        config.rules = mutableRuleList.toList()
    }

    fun addSubset(subset: Subset) {
        val mutableSubsetList = config.subsets.toMutableList()
        mutableSubsetList.add(subset)
        config.subsets = mutableSubsetList.toList()
    }

    @Subscribe
    fun onSubsetsUpdated(subsetsUpdatedEvent: SubsetsUpdated) {
        addSubset(subsetsUpdatedEvent.subset)
        addRuleSet(subsetsUpdatedEvent.ruleSet)
        addRules(subsetsUpdatedEvent.rules)

        postNewSubsets()
    }

    fun addStrata(strata: Strata) {
        val mutableStrataList = config.strata.toMutableList()
        mutableStrataList.add(strata)
        config.strata = mutableStrataList.toList()
    }

    @Subscribe
    fun onStrataUpdated(event: StrataUpdated) {
        addStrata(event.strata)
        addRuleSet(event.ruleSet)
        addRules(event.rules)

        postNewStrata()
    }

    @Subscribe
    fun onSamplingMethodChanged(event: SamplingMethodChanged) {
        config.samplingMethod = event.samplingMethod
    }

    private fun postNewSubsets() {
        val subsetsRuleSets = mutableListOf<RuleSet>()
        config.subsets.forEach { subset ->
            val ruleSetForSubset = config.ruleSets.find { ruleSet ->
                ruleSet.id == subset.ruleSetId
            }

            ruleSetForSubset?.let {
                subsetsRuleSets.add(it)
            }
        }

        val subsetsRuleRecords = mutableListOf<RuleRecord>()
        subsetsRuleSets.forEach { ruleSet ->
            val ruleRecordsForRuleSet = config.rules.filter {
                it.ruleSetId == ruleSet.id
            }

            subsetsRuleRecords.addAll(ruleRecordsForRuleSet)
        }

        eventBus.postSticky(AllSubsetsUpdated(config.subsets.toList(), subsetsRuleSets.toList(), subsetsRuleRecords.toList()))
    }

    private fun postNewStrata() {
        val strataRuleSets = mutableListOf<RuleSet>()
        config.strata.forEach { strata ->
            val ruleSetForStrata = config.ruleSets.find { ruleSet ->
                ruleSet.id == strata.ruleSetId
            }

            ruleSetForStrata?.let {
                strataRuleSets.add(it)
            }
        }

        val strataRuleRecords = mutableListOf<RuleRecord>()
        strataRuleSets.forEach { ruleSet ->
            val ruleRecordsForRuleSet = config.rules.filter {
                it.ruleSetId == ruleSet.id
            }

            strataRuleRecords.addAll(ruleRecordsForRuleSet)
        }

        eventBus.postSticky(AllStrataUpdated(config.strata.toList(), strataRuleSets.toList(), strataRuleRecords.toList()))
    }
    
    fun addEnumerationAreas(enumerationAreas: List<EnumerationArea>) {
        val mutableEnumerationAreasList = config.enumerationAreas.toMutableList()
        mutableEnumerationAreasList.addAll(enumerationAreas)
        config.enumerationAreas = mutableEnumerationAreasList.toList()
    }

    val photoCompressionOptions = arrayOf(100 to "No Compression", 50 to "Some Compression", 25 to "Maximum Compression")
}

class AllSubsetsUpdated(val subsets: List<Subset>, val ruleSets: List<RuleSet>, val ruleRecords: List<RuleRecord>)
class AllStrataUpdated(val subsets: List<Strata>, val ruleSets: List<RuleSet>, val ruleRecords: List<RuleRecord>)