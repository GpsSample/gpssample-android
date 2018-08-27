package org.taskforce.episample.filter

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.taskforce.episample.db.ConfigRoomDatabase
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.ResolvedEnumerationDao
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.metadata.CustomDropdown
import org.taskforce.episample.db.config.customfield.metadata.CustomFieldMetadata
import org.taskforce.episample.db.config.customfield.metadata.DropdownMetadata
import org.taskforce.episample.db.config.customfield.value.DropdownValue
import org.taskforce.episample.db.filter.Filter
import org.taskforce.episample.db.filter.dropdown.DropdownRuleFactory
import java.io.IOException
import java.util.*


@RunWith(AndroidJUnit4::class)
class DropdownFilterEnumerationTest {
    private val filterValue = CustomDropdown("Value 1")
    private val nonFilterValue = CustomDropdown("Value 2")

    private var configDao: ConfigDao? = null
    private var resolvedConfigDao: ResolvedConfigDao? = null
    private var studyDao: StudyDao? = null
    private var customFieldDao: CustomFieldDao? = null
    private var resolvedEnumerationDao: ResolvedEnumerationDao? = null
    private var db: ConfigRoomDatabase? = null
    lateinit var studyId: String
    lateinit var customField: CustomField

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(context, ConfigRoomDatabase::class.java).build()
        configDao = db?.configDao()
        studyDao = db?.studyDao()
        resolvedConfigDao = db?.resolvedConfigDao()
        customFieldDao = db?.customFieldDao()
        resolvedEnumerationDao = db?.resolvedEnumerationDao()

        val insertConfigId = UUID.randomUUID().toString()
        setupConfig(insertConfigId)

        val config = configDao!!.getConfigSync(insertConfigId)
        studyId = UUID.randomUUID().toString()
        val configId = "configId"

        val insertStudy = Study("Study 1", "Study Password", id = studyId)
        val insertStudyConfig = Config(config.name, Date(), studyId = insertStudy.id, id = configId)

        studyDao?.insert(insertStudy, insertStudyConfig, insertConfigId)

        customField = makeCustomField("some dropdown",
                CustomFieldType.DROPDOWN,
                DropdownMetadata(
                        listOf(
                                filterValue,
                                nonFilterValue
                        )
                ),
                configId
        )

        configDao?.insert(customField)

        for (i in 1..10) {
            val enumerationId = UUID.randomUUID().toString()
            val insertEnumeration = makeEnumeration(studyId, enumerationId)
            studyDao?.insert(insertEnumeration)

            val dropdownValue = DropdownValue(if (i % 5 == 0) filterValue.key else nonFilterValue.key)

            val insertFieldValue = CustomFieldValue(dropdownValue,
                    CustomFieldType.CHECKBOX,
                    enumerationId,
                    customField.id)

            customFieldDao?.insert(insertFieldValue)
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
    }

    /*
        Before you alter this, please note the following:
        All the following tests are based on a setup where a configuration with an dropdown custom field has been added, along with 10 enumerations
        where the custom field value which has been added is a dropdown with value == "Value 1" for the 5th enumeration and "Value 2" otherwise

        So for example, the test filterEqualTo is attempting to filter the results which are equal to "Value 1"
        So we would expect 1 enumeration

        And so on and so forth.
     */
    @Test
    @Throws(Exception::class)
    fun filterEqualTo() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(DropdownRuleFactory.makeRule(DropdownRuleFactory.DropdownRules.IS_EQUAL_TO, customField, filterValue.key)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filter(it)
        }

        Assert.assertEquals(1, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as DropdownValue).customDropdownId == filterValue.key)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterNotEqualTo() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(DropdownRuleFactory.makeRule(DropdownRuleFactory.DropdownRules.IS_NOT_EQUAL_TO, customField, filterValue.key)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filter(it)
        }

        Assert.assertEquals(9, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as DropdownValue).customDropdownId == filterValue.key)
        }
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
