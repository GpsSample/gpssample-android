package org.taskforce.episample.config.base

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.taskforce.episample.R
import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.config.fields.CustomFieldDefaultProvider
import org.taskforce.episample.config.fields.CustomFieldTypeConstants
import org.taskforce.episample.config.language.CustomLanguage
import org.taskforce.episample.config.language.LanguageService
import org.taskforce.episample.config.sampling.SamplingMethod
import org.taskforce.episample.config.settings.admin.AdminSettings
import org.taskforce.episample.config.settings.server.ServerSettings
import org.taskforce.episample.config.settings.user.UserSettings
import org.taskforce.episample.core.interfaces.EnumerationArea
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.RuleSet
import org.taskforce.episample.db.sampling.subsets.Subset
import org.taskforce.episample.fileImport.models.LandmarkType
import java.io.Serializable
import java.util.*

class ConfigBuildViewModel: ViewModel() {
    val configBuildManager = ConfigBuildManager()
}

class ConfigBuildManager(val config: Config = Config(Date())):
        CustomFieldDefaultProvider, Serializable {

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
    }
    
    fun addEnumerationAreas(enumerationAreas: List<EnumerationArea>) {
        val mutableEnumerationAreasList = config.enumerationAreas.toMutableList()
        mutableEnumerationAreasList.addAll(enumerationAreas)
        config.enumerationAreas = mutableEnumerationAreasList.toList()
    }

    val photoCompressionOptions = arrayOf(100 to "No Compression", 50 to "Some Compression", 25 to "Maximum Compression")
}
