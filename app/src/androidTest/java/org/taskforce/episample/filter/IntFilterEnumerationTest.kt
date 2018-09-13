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
import org.taskforce.episample.db.collect.ResolvedEnumerationDao
import org.taskforce.episample.db.config.ConfigDao
import org.taskforce.episample.db.config.ResolvedConfigDao
import org.taskforce.episample.db.config.Study
import org.taskforce.episample.db.config.StudyDao
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.metadata.NumberMetadata
import org.taskforce.episample.db.config.customfield.value.IntValue
import org.taskforce.episample.db.filter.Filter
import org.taskforce.episample.db.filter.integers.IntRuleFactory
import org.taskforce.episample.db.utils.CommonSetup.Companion.makeCustomField
import org.taskforce.episample.db.utils.CommonSetup.Companion.makeEnumeration
import org.taskforce.episample.db.utils.CommonSetup.Companion.setupConfig
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class IntFilterEnumerationTest {
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
        setupConfig(configDao!!, insertConfigId)

        studyId = UUID.randomUUID().toString()

        val insertStudy = Study("Study 1", "Study Password", id = studyId)
        studyDao?.insert(insertStudy, insertConfigId)

        val numberMetadata = NumberMetadata(true)
        customField = makeCustomField("name",
                CustomFieldType.NUMBER,
                numberMetadata,
                insertConfigId
        )

        configDao?.insert(customField)

        for (i in 1..10) {
            val enumerationId = UUID.randomUUID().toString()
            val insertEnumeration = makeEnumeration(studyId, enumerationId)
            studyDao?.insert(insertEnumeration)

            val intValue = org.taskforce.episample.db.config.customfield.value.IntValue(i)

            val insertFieldValue = CustomFieldValue(intValue,
                    CustomFieldType.NUMBER,
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
        All the following tests are based on a setup where a configuration with an integer custom field has been added, along with 10 enumerations
        where the custom field value which has been added is an integer taking the values [1..10] have been added

        So for example, the test filterLessThan is attempting to filter the results less than the value 5
        So we would expect enumerations with the following values to be found [1, 2, 3, 4] for a size of 4

        For filterGreaterThanEqualTo, it is attempting to filter results that are greater than or equal to 5
        So we would expect enumerations with the following values to be found [5, 6, 7, 8, 9, 10] for a size of 6

        And so on and so forth.
     */

    @Test
    @Throws(Exception::class)
    fun filterLessThan() {
        val filterValue = 5

        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(IntRuleFactory.makeRule(IntRuleFactory.Rules.LESS_THAN, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(4, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as IntValue).intValue < filterValue)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterLessThanEqualTo() {
        val filterValue = 5

        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(IntRuleFactory.makeRule(IntRuleFactory.Rules.LESS_THAN_OR_EQUAL_TO, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(5, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as IntValue).intValue <= filterValue)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterGreaterThan() {
        val filterValue = 5

        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(IntRuleFactory.makeRule(IntRuleFactory.Rules.GREATER_THAN, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(5, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as IntValue).intValue > filterValue)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterGreaterThanEqualTo() {
        val filterValue = 5

        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(IntRuleFactory.makeRule(IntRuleFactory.Rules.GREATER_THAN_OR_EQUAL_TO, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(6, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as IntValue).intValue >= filterValue)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterEqualTo() {
        val filterValue = 5

        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(IntRuleFactory.makeRule(IntRuleFactory.Rules.IS_EQUAL_TO, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(1, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as IntValue).intValue == filterValue)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterNotEqualTo() {
        val filterValue = 5

        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(IntRuleFactory.makeRule(IntRuleFactory.Rules.IS_NOT_EQUAL_TO, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(9, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as IntValue).intValue != filterValue)
        }
    }
}
