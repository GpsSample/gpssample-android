package org.taskforce.episample.db.dao

import android.arch.persistence.room.Room
import android.database.sqlite.SQLiteConstraintException
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.taskforce.episample.config.fields.CustomFieldTypeConstants
import org.taskforce.episample.db.ConfigRoomDatabase
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreakcrumbDao
import org.taskforce.episample.db.collect.ResolvedEnumerationDao
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.metadata.*
import org.taskforce.episample.db.config.customfield.value.*
import org.taskforce.episample.utils.makeDBConfig
import java.io.IOException
import java.util.*


@RunWith(AndroidJUnit4::class)
class SimpleEntityReadWriteTest {
    private var configDao: ConfigDao? = null
    private var resolvedConfigDao: ResolvedConfigDao? = null
    private var studyDao: StudyDao? = null
    private var resolvedStudyDao: ResolvedStudyDao? = null
    private var breadcrumbDao: GpsBreakcrumbDao? = null
    private var customFieldDao: CustomFieldDao? = null
    private var resolvedEnumerationDao: ResolvedEnumerationDao? = null
    private var db: ConfigRoomDatabase? = null

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(context, ConfigRoomDatabase::class.java).build()
        configDao = db!!.configDao()
        studyDao = db!!.studyDao()
        resolvedConfigDao = db!!.resolvedConfigDao()
        breadcrumbDao = db!!.breadcrumbDao()
        resolvedStudyDao = db!!.resolvedStudyDao()
        customFieldDao = db!!.customFieldDao()
        resolvedEnumerationDao = db!!.resolvedEnumerationDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db!!.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeConfigAndReadInList() {
        val config1 = Config("Config 1")
        val config2 = Config("Config 2")
        val config3 = Config("Config 2")
        configDao?.insert(config1)
        configDao?.insert(config2)
        configDao?.insert(config3)
        val allConfigs = configDao?.getAllConfigsSync()

        assertEquals(allConfigs!!.size, 3)
    }

    @Test
    @Throws(Exception::class)
    fun writeConfigRelationsAndReadInResolvedConfig() {
        val configName = "Config 1"
        val enumerationSingular = "Person"
        val enumerationPlural = "People"
        val enumerationLabel = "Point of Contact"
        val adminPassword = "anypassword"

        val config = Config(configName)
        configDao?.insert(config,
                listOf(),
                org.taskforce.episample.db.config.AdminSettings(adminPassword, config.id),
                EnumerationSubject(enumerationSingular, enumerationPlural, enumerationLabel, config.id))

        val resolvedConfigs = resolvedConfigDao!!.getConfigSync(config.id)
        assertEquals(1, resolvedConfigs.size)
        assertEquals(configName, resolvedConfigs[0].name)
        assertEquals(adminPassword, resolvedConfigs[0].adminSettings.password)
        assertEquals(enumerationSingular, resolvedConfigs[0].enumerationSubject.singular)
        assertEquals(enumerationPlural, resolvedConfigs[0].enumerationSubject.plural)
        assertEquals(enumerationLabel, resolvedConfigs[0].enumerationSubject.primaryLabel)
    }

    @Test(expected = SQLiteConstraintException::class)
    @Throws(Exception::class)
    fun writingMoreThanOneAdminSettingsToAConfigThrowsException() {
        val configName = "Config 1"
        val adminPassword = "anypassword"
        val config = Config(configName)
        configDao?.insert(config,
                listOf(),
                org.taskforce.episample.db.config.AdminSettings(adminPassword, config.id),
                EnumerationSubject("singular", "plural", "primaryLabel", config.id))

        configDao?.insert(org.taskforce.episample.db.config.AdminSettings("foo", config.id))
    }

    @Test(expected = SQLiteConstraintException::class)
    @Throws(Exception::class)
    fun writingMoreThanOneEnumerationSubjectsToAConfigThrowsException() {
        val configName = "Config 1"
        val adminPassword = "anypassword"
        val config = Config(configName)
        configDao?.insert(config,
                listOf(),
                AdminSettings(adminPassword, config.id),
                EnumerationSubject("singular", "plural", "primaryLabel", config.id))

        configDao?.insert(EnumerationSubject("singular", "plural", "primaryLabel", config.id))
    }

    @Test
    @Throws
    fun duplicateConfig() {
        val configName = "Config 1"
        val dupeName = "Dupe Name"
        val enumerationSingular = "Person"
        val enumerationPlural = "People"
        val enumerationLabel = "Point of Contact"
        val adminPassword = "anypassword"

        val insertConfig = Config(configName)
        configDao?.insert(
                insertConfig,
                listOf(),
                AdminSettings(adminPassword, insertConfig.id),
                EnumerationSubject(enumerationSingular, enumerationPlural, enumerationLabel, insertConfig.id)
        )


        val dupeId = configDao!!.duplicate(insertConfig, "Dupe Name")

        val resolvedConfig = resolvedConfigDao!!.getConfigSync(dupeId).first()

        assertNotEquals(dupeId, insertConfig.id)
        assertEquals(dupeName, resolvedConfig.name)
        assertEquals(adminPassword, resolvedConfig.adminSettings?.password)
        assertEquals(enumerationSingular, resolvedConfig.enumerationSubject.singular)
        assertEquals(enumerationPlural, resolvedConfig.enumerationSubject.plural)
        assertEquals(enumerationLabel, resolvedConfig.enumerationSubject.primaryLabel)
    }

    @Test
    @Throws(Exception::class)
    fun insertStudy() {
        val configName = "Config 1"
        val enumerationSingular = "Person"
        val enumerationPlural = "People"
        val enumerationLabel = "Point of Contact"
        val adminPassword = "anypassword"

        val insertConfig = Config(configName)
        configDao?.insert(
                insertConfig,
                listOf(),
                AdminSettings(adminPassword, insertConfig.id),
                EnumerationSubject(enumerationSingular, enumerationPlural, enumerationLabel, insertConfig.id
                )

        )

        val config = configDao!!.getConfigSync(insertConfig.id)

        val studyName = "Study 1"
        val studyPassword = "Study Password"
        val insertStudy = Study(studyName, studyPassword)
        val insertStudyConfig = Config(config.name, Date(), studyId = insertStudy.id)
        studyDao?.insert(insertStudy, insertStudyConfig, config.id)

        val study = studyDao!!.getStudySync(insertStudy.id)
        assertEquals(studyName, study.name)
        assertEquals(studyPassword, study.password)

        val studyConfig = configDao!!.getAllConfigsSync().first { it.studyId == study.id }
        assertEquals(configName, studyConfig.name)
    }

    @Test
    @Throws(Exception::class)
    fun insertCustomDropdownField() {
        val config = Config("Config 1")
        configDao?.insert(config)

        val dropdownSource = listOf(
                CustomDropdown("value 1"),
                CustomDropdown("value 2")
        )

        val dropdownMetadata = DropdownMetadata(dropdownSource)

        val insertField = makeCustomField("name",
                CustomFieldType.DROPDOWN,
                dropdownMetadata,
                config.id
        )
        configDao?.insert(insertField)

        val dropdownField = customFieldDao!!.getFieldSync(insertField.id).first()

        assertTrue(dropdownField.metadata is DropdownMetadata)
    }

    @Test
    @Throws(Exception::class)
    fun insertCustomDateField() {
        val config = Config("Config 1")
        configDao?.insert(config)

        val dateMetadata = DateMetadata(true, false, true, false)
        val insertField = makeCustomField("name",
                CustomFieldType.DATE,
                dateMetadata,
                config.id
        )
        configDao?.insert(insertField)

        val dateField = customFieldDao!!.getFieldSync(insertField.id).first()

        assertTrue(dateField.metadata is DateMetadata)
    }

    @Test
    @Throws(Exception::class)
    fun insertCustomNumberField() {
        val config = Config("Config 1")
        configDao?.insert(config)

        val numberMetadata = NumberMetadata(false)
        val insertField = makeCustomField("name",
                CustomFieldType.NUMBER,
                numberMetadata,
                config.id
        )

        configDao?.insert(insertField)

        val numberField = customFieldDao!!.getFieldSync(insertField.id).first()

        assertTrue(numberField.metadata is NumberMetadata)
    }

    @Test
    @Throws(Exception::class)
    fun insertVariableNumberOfFields() {
        val config = Config("Config 1")
        configDao?.insert(config)

        val numberMetadata = NumberMetadata(false)
        val insertField = makeCustomField("name",
                CustomFieldType.NUMBER,
                numberMetadata,
                config.id
        )
        val insertField2 = makeCustomField("name",
                CustomFieldType.DROPDOWN,
                DropdownMetadata(items = listOf(CustomDropdown("title"))),
                config.id
        )

        configDao?.insert(insertField, insertField2)


        val numberField = customFieldDao!!.getFieldSync(insertField.id).first()

        assertTrue(numberField.metadata is NumberMetadata)

        val dropdownField = customFieldDao!!.getFieldSync(insertField2.id).first()

        assertTrue(dropdownField.metadata is DropdownMetadata)
    }

    @Test
    @Throws(Exception::class)
    fun insertCustomDoubleValue() {
        val enumerationId = "enumerationId"
        val configId = "configId"
        setupEnumeration(enumerationId, configId)

        val numberMetadata = NumberMetadata(false)
        val insertField = makeCustomField("name",
                CustomFieldType.NUMBER,
                numberMetadata,
                configId
        )

        configDao?.insert(insertField)

        val expectedDoubleValue = 1.1
        val doubleValue = org.taskforce.episample.db.config.customfield.value.DoubleValue(expectedDoubleValue)

        val insertFieldValue = CustomFieldValue(doubleValue,
                CustomFieldType.NUMBER,
                enumerationId,
                insertField.id)

        customFieldDao?.insert(insertFieldValue)

        val resolvedEnumerations = resolvedEnumerationDao?.getEnumerationSync(enumerationId)

        assertEquals(1, resolvedEnumerations?.size)
        assertEquals(1, resolvedEnumerations!![0].customFieldValues.size)
        assertEquals(expectedDoubleValue, (resolvedEnumerations[0].customFieldValues[0].value as DoubleValue).doubleValue, 0.01)
    }

    @Test
    @Throws(Exception::class)
    fun insertCustomIntValue() {
        val enumerationId = "enumerationId"
        val configId = "configId"
        setupEnumeration(enumerationId, configId)

        val numberMetadata = NumberMetadata(true)
        val insertField = makeCustomField("name",
                CustomFieldType.NUMBER,
                numberMetadata,
                configId
        )

        configDao?.insert(insertField)

        val expectedIntValue = 1
        val intValue = org.taskforce.episample.db.config.customfield.value.IntValue(expectedIntValue)

        val insertFieldValue = CustomFieldValue(intValue,
                CustomFieldType.NUMBER,
                enumerationId,
                insertField.id)

        customFieldDao?.insert(insertFieldValue)

        val resolvedEnumerations = resolvedEnumerationDao?.getEnumerationSync(enumerationId)

        assertEquals(1, resolvedEnumerations?.size)
        assertEquals(1, resolvedEnumerations!![0].customFieldValues.size)
        assertEquals(expectedIntValue, (resolvedEnumerations[0].customFieldValues[0].value as IntValue).intValue)
    }

    @Test
    @Throws(Exception::class)
    fun insertCustomBooleanValue() {
        val enumerationId = "enumerationId"
        val configId = "configId"
        setupEnumeration(enumerationId, configId)

        val insertField = makeCustomField("name",
                CustomFieldType.CHECKBOX,
                EmptyMetadata(),
                configId
        )

        configDao?.insert(insertField)

        val boolValue = org.taskforce.episample.db.config.customfield.value.BooleanValue(true)

        val insertFieldValue = CustomFieldValue(boolValue,
                CustomFieldType.CHECKBOX,
                enumerationId,
                insertField.id)

        customFieldDao?.insert(insertFieldValue)

        val resolvedEnumerations = resolvedEnumerationDao?.getEnumerationSync(enumerationId)

        assertEquals(1, resolvedEnumerations?.size)
        assertEquals(1, resolvedEnumerations!![0].customFieldValues.size)
        assertTrue((resolvedEnumerations[0].customFieldValues[0].value as BooleanValue).boolValue)
    }

    @Test
    @Throws(Exception::class)
    fun insertCustomDateValue() {
        val enumerationId = "enumerationId"
        val configId = "configId"
        setupEnumeration(enumerationId, configId)

        val insertField = makeCustomField("name",
                CustomFieldType.DATE,
                DateMetadata(true, false, true, false),
                configId
        )

        configDao?.insert(insertField)

        val date = Date()
        val dateValue = org.taskforce.episample.db.config.customfield.value.DateValue(date)

        val insertFieldValue = CustomFieldValue(dateValue,
                CustomFieldType.DATE,
                enumerationId,
                insertField.id
        )

        customFieldDao?.insert(insertFieldValue)

        val resolvedEnumerations = resolvedEnumerationDao?.getEnumerationSync(enumerationId)

        assertEquals(1, resolvedEnumerations?.size)
        assertEquals(1, resolvedEnumerations!![0].customFieldValues.size)
        assertEquals(date, (resolvedEnumerations[0].customFieldValues[0].value as DateValue).dateValue)
    }

    @Test
    @Throws(Exception::class)
    fun insertCustomDropdownValue() {
        val enumerationId = "enumerationId"
        val configId = "configId"
        setupEnumeration(enumerationId, configId)

        val option1 = CustomDropdown("Option1")
        val option2 = CustomDropdown("Option2")
        val insertField = makeCustomField("name",
                CustomFieldType.DROPDOWN,
                DropdownMetadata(listOf(option1, option2)),
                configId
        )

        configDao?.insert(insertField)

        val dropdownValue = org.taskforce.episample.db.config.customfield.value.DropdownValue(option1.key)

        val insertFieldValue = CustomFieldValue(dropdownValue,
                CustomFieldType.DROPDOWN,
                enumerationId,
                insertField.id)

        customFieldDao?.insert(insertFieldValue)

        val resolvedEnumerations = resolvedEnumerationDao?.getEnumerationSync(enumerationId)

        assertEquals(1, resolvedEnumerations?.size)
        assertEquals(1, resolvedEnumerations!![0].customFieldValues.size)
        assertEquals(option1.key, (resolvedEnumerations[0].customFieldValues[0].value as DropdownValue).customDropdownId)
    }

    @Test
    @Throws(Exception::class)
    fun insertCustomTextValue() {
        val enumerationId = "enumerationId"
        val configId = "configId"
        setupEnumeration(enumerationId, configId)

        val insertField = makeCustomField("name",
                CustomFieldType.TEXT,
                EmptyMetadata(),
                configId
        )

        configDao?.insert(insertField)

        val expectedText = "filled out value"
        val textValue = org.taskforce.episample.db.config.customfield.value.TextValue(expectedText)

        val insertFieldValue = CustomFieldValue(textValue,
                CustomFieldType.TEXT,
                enumerationId,
                insertField.id)

        customFieldDao?.insert(insertFieldValue)

        val resolvedEnumerations = resolvedEnumerationDao?.getEnumerationSync(enumerationId)

        assertEquals(1, resolvedEnumerations?.size)
        assertEquals(1, resolvedEnumerations!![0].customFieldValues.size)
        assertEquals(expectedText, (resolvedEnumerations[0].customFieldValues[0].value as TextValue).text)
    }

    @Test
    fun addCustomIntField() {
        val configId = UUID.randomUUID().toString()

        val expectedFieldValues = addCustomField(
                configId = configId,
                type = CustomFieldType.NUMBER,
                metadata = NumberMetadata(isIntegerOnly = true),
                properties = mapOf(
                        CustomFieldTypeConstants.INTEGER_ONLY to true
                )
        )

        val resolvedConfig = resolvedConfigDao!!.getConfigSync(configId)
        val actualField = resolvedConfig.first().customFields.first()
        assertTrue((actualField.metadata as NumberMetadata).isIntegerOnly)
        assertEquals(expectedFieldValues.configId, actualField.configId)
    }

    @Test
    fun addCustomDoubleField() {
        val configId = UUID.randomUUID().toString()

        val expectedFieldValues = addCustomField(
                configId = configId,
                type = CustomFieldType.NUMBER,
                metadata = NumberMetadata(isIntegerOnly = false),
                properties = mapOf(
                        CustomFieldTypeConstants.INTEGER_ONLY to false
                )
        )

        val resolvedConfig = resolvedConfigDao!!.getConfigSync(configId)
        val actualField = resolvedConfig.first().customFields.first()
        assertFalse((actualField.metadata as NumberMetadata).isIntegerOnly)
        assertEquals(expectedFieldValues.configId, actualField.configId)
    }

    @Test
    fun addCustomDateField() {
        val configId = UUID.randomUUID().toString()

        val expectedYear = true
        val expectedMonth = true
        val expectedDay = false
        val expectedTime = false

        val expectedFieldValues = addCustomField(
                configId = configId,
                type = CustomFieldType.DATE,
                metadata = NumberMetadata(isIntegerOnly = false),
                properties = mapOf(
                        CustomFieldTypeConstants.YEAR to expectedYear,
                        CustomFieldTypeConstants.MONTH to expectedMonth,
                        CustomFieldTypeConstants.DAY to expectedDay,
                        CustomFieldTypeConstants.TIME to expectedTime
                )
        )

        val resolvedConfig = resolvedConfigDao!!.getConfigSync(configId)
        val actualField = resolvedConfig.first().customFields.first()
        val metadata = actualField.metadata as DateMetadata
        assertEquals(expectedYear, metadata.showYear)
        assertEquals(expectedMonth, metadata.showMonth)
        assertEquals(expectedDay, metadata.showDay)
        assertEquals(expectedTime, metadata.showTime)
        assertEquals(expectedFieldValues.configId, actualField.configId)
    }

    @Test
    fun addCustomCheckboxField() {
        val configId = UUID.randomUUID().toString()

        val expectedFieldValues = addCustomField(
                configId = configId,
                type = CustomFieldType.CHECKBOX,
                metadata = EmptyMetadata(),
                properties = mapOf()
        )

        val resolvedConfig = resolvedConfigDao!!.getConfigSync(configId)
        val actualField = resolvedConfig.first().customFields.first()
        assertTrue(actualField.metadata is EmptyMetadata)
        assertEquals(expectedFieldValues.configId, actualField.configId)
    }

    @Test
    fun addCustomTextField() {
        val configId = UUID.randomUUID().toString()

        val expectedFieldValues = addCustomField(
                configId = configId,
                type = CustomFieldType.TEXT,
                metadata = EmptyMetadata(),
                properties = mapOf()
        )

        val resolvedConfig = resolvedConfigDao!!.getConfigSync(configId)
        val actualField = resolvedConfig.first().customFields.first()
        assertTrue(actualField.metadata is EmptyMetadata)
        assertEquals(expectedFieldValues.configId, actualField.configId)
    }

    @Test
    fun addCustomDropdownField() {
        val configId = UUID.randomUUID().toString()

        val expectedKey1 = "key 1"
        val expectedKey2 = "key 2"
        val expectedValue1 = "option 1"
        val expectedValue2 = "option 2"

        val expectedFieldValues = addCustomField(
                configId = configId,
                type = CustomFieldType.DROPDOWN,
                metadata = EmptyMetadata(),
                properties = mapOf(
                        CustomFieldTypeConstants.DROPDOWN_ITEMS to listOf(
                                org.taskforce.episample.config.fields.CustomDropdown(expectedValue1, expectedKey1),
                                org.taskforce.episample.config.fields.CustomDropdown(expectedValue2, expectedKey2)
                        )
                )
        )

        val resolvedConfig = resolvedConfigDao!!.getConfigSync(configId)
        val actualField = resolvedConfig.first().customFields.first()
        val option1 = (actualField.metadata as DropdownMetadata).items[0]
        val option2 = (actualField.metadata as DropdownMetadata).items[1]
        assertEquals(expectedKey1, option1.key)
        assertEquals(expectedKey2, option2.key)
        assertEquals(expectedValue1, option1.value)
        assertEquals(expectedValue2, option2.value)
        assertEquals(expectedFieldValues.configId, actualField.configId)
    }

    @Test
    @Throws(Exception::class)
    fun insertConfigWithCustomFields() {
        val config = Config("Config 1")

        val dropdownSource = listOf(
                CustomDropdown("value 1"),
                CustomDropdown("value 2")
        )

        val dropdownMetadata = DropdownMetadata(dropdownSource)

        val insertField = makeCustomField("name",
                CustomFieldType.DROPDOWN,
                dropdownMetadata,
                config.id
        )

        configDao?.insert(config,
                listOf(insertField),
                AdminSettings("anypassword", config.id),
                EnumerationSubject("Person", "People", "Point of Contact", config.id)
        )

        val resolvedConfig = resolvedConfigDao?.getConfigSync(config.id)
        assertEquals(1, resolvedConfig?.size)
    }

    private fun setupConfig(configId: String) {
        val insertConfig = Config("Config 1", id = configId)
        configDao?.insert(
                insertConfig,
                listOf(),
                AdminSettings("anypassword", insertConfig.id),
                EnumerationSubject("Person", "People", "Point of Contact", insertConfig.id)
        )
    }

    private fun setupEnumeration(enumerationId: String, configId: String, studyId: String = UUID.randomUUID().toString()) {
        val insertConfigId = UUID.randomUUID().toString()
        setupConfig(insertConfigId)

        val config = configDao!!.getConfigSync(insertConfigId)

        val insertStudy = Study("Study 1", "Study Password", id = studyId)
        val insertStudyConfig = Config(config.name, Date(), studyId = insertStudy.id, id = configId)

        studyDao?.insert(insertStudy, insertStudyConfig, insertConfigId)

        val insertEnumeration = makeEnumeration(studyId, enumerationId)
        studyDao?.insert(insertEnumeration)
    }

    private fun addCustomField(configId: String, type: CustomFieldType, metadata: CustomFieldMetadata, properties: Map<String, Any>): CustomField {
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

        configDao?.insert(Config("Config Name", id = configId),
                listOf(),
                org.taskforce.episample.db.config.AdminSettings("password", configId),
                EnumerationSubject("s", "p", "l", configId))

        configDao?.insert(*convertedFields.toTypedArray())

        return expectedFieldValues
    }

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

        fun makeEnumeration(studyId: String, enumerationId: String): Enumeration {
            return Enumeration("Jesse",
                    0.0,
                    0.0,
                    null,
                    false,
                    10.0,
                    studyId,
                    id = enumerationId
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
