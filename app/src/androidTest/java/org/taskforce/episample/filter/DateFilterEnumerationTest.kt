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
import org.taskforce.episample.db.StudyRoomDatabase
import org.taskforce.episample.db.collect.ResolvedEnumerationDao
import org.taskforce.episample.db.config.ConfigDao
import org.taskforce.episample.db.config.ResolvedConfigDao
import org.taskforce.episample.db.config.Study
import org.taskforce.episample.db.config.StudyDao
import org.taskforce.episample.db.config.customfield.*
import org.taskforce.episample.db.config.customfield.metadata.NumberMetadata
import org.taskforce.episample.db.config.customfield.value.DateValue
import org.taskforce.episample.db.filter.Filter
import org.taskforce.episample.db.filter.date.DateRuleFactory
import org.taskforce.episample.db.utils.CommonSetup.Companion.makeCustomField
import org.taskforce.episample.db.utils.CommonSetup.Companion.makeEnumeration
import org.taskforce.episample.db.utils.CommonSetup.Companion.setupConfig
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class DateFilterEnumerationTest {
    private val filterValueMillisecondsSinceEpoch = 1_500_000_000_000L
    private val filterValue = Date(filterValueMillisecondsSinceEpoch)
    private var configDao: ConfigDao? = null
    private var resolvedConfigDao: ResolvedConfigDao? = null
    private var studyDao: StudyDao? = null
    private var customFieldDao: CustomFieldDao? = null
    private var customFieldValueDao: CustomFieldValueDao? = null
    private var resolvedEnumerationDao: ResolvedEnumerationDao? = null
    private var db: StudyRoomDatabase? = null
    lateinit var studyId: String
    lateinit var customField: CustomField

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(context, StudyRoomDatabase::class.java).build()
        configDao = db?.configDao()
        studyDao = db?.studyDao()
        resolvedConfigDao = db?.resolvedConfigDao()
        customFieldDao = db?.customFieldDao()
        customFieldValueDao = db?.customFieldValueDao()
        resolvedEnumerationDao = db?.resolvedEnumerationDao()

        val insertConfigId = UUID.randomUUID().toString()
        setupConfig(configDao!!, insertConfigId)

        val resolvedConfig = resolvedConfigDao!!.getConfigSync(insertConfigId)
        studyId = UUID.randomUUID().toString()

        val studyConfigId = studyDao!!.insert(studyId, "Study 1", "Study Password", resolvedConfig)

        val numberMetadata = NumberMetadata(false)
        customField = makeCustomField("name",
                CustomFieldType.NUMBER,
                numberMetadata,
                studyConfigId
        )

        configDao?.insert(customField)

        for (i in 1..10) {
            val enumerationId = UUID.randomUUID().toString()
            val insertEnumeration = makeEnumeration(studyId, enumerationId)
            studyDao?.insert(insertEnumeration)

            val millisSinceEpoch = 1_000_000_000_000L + 100_000_000_000L * i
            val dateForInsert = DateValue(Date(millisSinceEpoch))

            val insertFieldValue = CustomFieldValue(dateForInsert,
                    CustomFieldType.DATE,
                    enumerationId,
                    customField.id)

            customFieldValueDao?.insert(insertFieldValue)
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
        where the custom field value which has been added is a Date taking the values [1..10]*100Billion + 1 Trillion (for millis since epoch) have been added

        The date we are filtering against is 1.5 trillion millis since epoch.

        And so on and so forth.
     */

    @Test
    @Throws(Exception::class)
    fun filterLessThan() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(DateRuleFactory.makeRule(DateRuleFactory.Rules.LESS_THAN, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(4, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as DateValue).dateValue < filterValue)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterLessThanEqualTo() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(DateRuleFactory.makeRule(DateRuleFactory.Rules.LESS_THAN_OR_EQUAL_TO, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(5, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as DateValue).dateValue<= filterValue)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterGreaterThan() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(DateRuleFactory.makeRule(DateRuleFactory.Rules.GREATER_THAN, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(5, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as DateValue).dateValue > filterValue)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterGreaterThanEqualTo() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(DateRuleFactory.makeRule(DateRuleFactory.Rules.GREATER_THAN_OR_EQUAL_TO, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(6, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as DateValue).dateValue >= filterValue)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterEqualTo() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(DateRuleFactory.makeRule(DateRuleFactory.Rules.IS_EQUAL_TO, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(1, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as DateValue).dateValue == filterValue)
        }
    }

    @Test
    @Throws(Exception::class)
    fun filterNotEqualTo() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filterLessThan = Filter(listOf(DateRuleFactory.makeRule(DateRuleFactory.Rules.IS_NOT_EQUAL_TO, customField, filterValue)))

        val filteredEnumerations = resolvedEnumerations?.let {
            filterLessThan.filterAny(it)
        }

        Assert.assertEquals(9, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            Assert.assertTrue((it.customFieldValues.first().value as DateValue).dateValue != filterValue)
        }
    }
}
