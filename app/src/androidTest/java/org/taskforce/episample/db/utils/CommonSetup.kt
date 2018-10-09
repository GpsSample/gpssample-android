package org.taskforce.episample.db.utils

import android.content.Context
import org.taskforce.episample.config.base.Config
import org.taskforce.episample.config.fields.CustomFieldTypeConstants
import org.taskforce.episample.config.settings.admin.AdminSettings
import org.taskforce.episample.core.interfaces.LiveEnumerationSubject
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.metadata.CustomFieldMetadata
import org.taskforce.episample.managers.LiveConfig
import org.taskforce.episample.utils.makeDBConfig
import java.util.*

class CommonSetup {
    companion object {
        fun makeCustomField(name: String,
                            type: CustomFieldType,
                            metadata: CustomFieldMetadata,
                            configId: String): CustomField {
            return CustomField(name,
                    type,
                    isAutomatic = false,
                    isPrimary = false,
                    shouldExport = false,
                    isRequired = false,
                    isPersonallyIdentifiableInformation = false,
                    metadata = metadata,
                    configId = configId)
        }

        fun makeUserSettings(configId: String, gpsMinimumPrecision: Double = 40.0, gpsPreferredPrecision: Double = 20.0, allowPhotos: Boolean = true, photoCompressionScale: Int = 50): UserSettings {
            return UserSettings(gpsMinimumPrecision, gpsPreferredPrecision, allowPhotos, photoCompressionScale,configId)
        }

        fun makeDisplaySettings(configId: String, isMetricDate: Boolean = true, is24HourTime: Boolean = false): DisplaySettings {
            return DisplaySettings(isMetricDate, is24HourTime, configId)
        }

        fun makeEnumeration(studyId: String, enumerationId: String): Enumeration {
            return Enumeration("Jesse",
                    0.0,
                    0.0,
                    null,
                    false,
                    false,
                    10.0,
                    studyId,
                    id = enumerationId
            )
        }

        fun setupConfigAndStudy(context: Context,
                                configRepository: ConfigRepository,
                                studyRepository: StudyRepository,
                                configName: String = "Config 1",
                                enumerationSingular: String = "Person",
                                enumerationPlural: String = "People",
                                enumerationLabel: String = "Name of Person",
                                adminPassword: String = "anypassword",
                                customFields: List<org.taskforce.episample.config.fields.CustomField> = listOf(org.taskforce.episample.config.fields.CustomField(true, true, true, true, true, "Custom Number", CustomFieldType.NUMBER,
                                        mapOf(CustomFieldTypeConstants.INTEGER_ONLY to true))),
                                customLandmarkTypes: List<Config.CustomLandmarkTypeInput> = listOf(),
                                gpsMinimumPrecision: Double = 40.0,
                                gpsPreferredPrecision: Double = 20.0,
                                isMetricDate: Boolean = true,
                                is24HourTime: Boolean = true,
                                callback: (config: org.taskforce.episample.core.interfaces.Config, studyId: String) -> Unit) {
            val configBuilder = org.taskforce.episample.config.base.Config(name = configName)
            configBuilder.adminSettings = AdminSettings(adminPassword)
            configBuilder.enumerationSubject = LiveEnumerationSubject(enumerationSingular, enumerationPlural, enumerationLabel)
            configBuilder.customFields = customFields
            configBuilder.userSettings = org.taskforce.episample.config.settings.user.UserSettings(gpsMinimumPrecision, gpsPreferredPrecision,
                    false, null, false, false, null, false, false, null)
            configBuilder.displaySettings = org.taskforce.episample.config.settings.display.DisplaySettings(isMetricDate, is24HourTime)
            configBuilder.customLandmarkTypes = customLandmarkTypes

            configRepository.insertConfigFromBuildManager(configBuilder) {
                val config = configRepository.getResolvedConfigSync(it)
                configRepository.insertStudy(config, "Study Name", "Study Password", { configId, studyId ->
                    // get study Config from repository
                    val studyConfig = LiveConfig(context, studyRepository.getResolvedConfigSync(configId))
                    callback(studyConfig, studyId)
                })
            }
        }

        fun setupEnumeration(configDao: ConfigDao, resolvedConfigDao: ResolvedConfigDao, studyDao: StudyDao, enumerationId: String, configId: String, studyId: String = UUID.randomUUID().toString()) {
            setupConfig(configDao, configId)

            val resolvedConfig = resolvedConfigDao.getConfigSync(configId)
            val studyId = UUID.randomUUID().toString()

            studyDao?.insert(studyId, "Study 1", "Study Password", resolvedConfig)

            val insertEnumeration = CommonSetup.makeEnumeration(studyId, enumerationId)
            studyDao.insert(insertEnumeration)
        }

        fun addCustomField(configDao: ConfigDao, configId: String, type: CustomFieldType, metadata: CustomFieldMetadata, properties: Map<String, Any>): CustomField {
            val expectedFieldValues = CustomField(
                    name = "Number Field Name",
                    type = type,
                    isAutomatic = false,
                    isPrimary = true,
                    shouldExport = false,
                    isPersonallyIdentifiableInformation = true,
                    metadata = metadata,
                    configId = configId,
                    isRequired = false)

            val customFields = listOf(
                    expectedFieldValues.makeCustomField(properties)
            )

            val convertedFields = customFields.map {
                it.makeDBConfig(configId)

            }

            configDao.insert(org.taskforce.episample.db.config.Config("Config Name", id = configId),
                    listOf(),
                    listOf(),
                    org.taskforce.episample.db.config.AdminSettings("password", configId),
                    EnumerationSubject("s", "p", "l", configId),
                    CommonSetup.makeUserSettings(configId),
                    CommonSetup.makeDisplaySettings(configId),
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf())

            configDao.insert(*convertedFields.toTypedArray())

            return expectedFieldValues
        }

        fun setupConfig(configDao: ConfigDao, config: org.taskforce.episample.db.config.Config, adminPassword: String, enumerationSingular: String, enumerationPlural: String, enumerationLabel: String) {
            configDao.insert(config,
                    listOf(),
                    listOf(),
                    AdminSettings(adminPassword, config.id),
                    EnumerationSubject(enumerationSingular, enumerationPlural, enumerationLabel, config.id),
                    CommonSetup.makeUserSettings(config.id),
                    CommonSetup.makeDisplaySettings(config.id),
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf()
            )
        }

        fun setupConfig(configDao: ConfigDao, configId: String = UUID.randomUUID().toString()) {
            val insertConfig = Config("Config 1", id = configId)
            configDao.insert(
                    insertConfig,
                    listOf(),
                    listOf(),
                    AdminSettings("anypassword", insertConfig.id),
                    EnumerationSubject("Person", "People", "Point of Contact", insertConfig.id),
                    CommonSetup.makeUserSettings(configId),
                    CommonSetup.makeDisplaySettings(insertConfig.id),
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf()
            )
        }
    }
}

fun CustomField.makeCustomField(properties: Map<String, Any>): org.taskforce.episample.config.fields.CustomField {
    return org.taskforce.episample.config.fields.CustomField(
            isAutomatic,
            isPrimary,
            shouldExport,
            isRequired,
            isPersonallyIdentifiableInformation,
            name,
            type,
            properties,
            id)
}
