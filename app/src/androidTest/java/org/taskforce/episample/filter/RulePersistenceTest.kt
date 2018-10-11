package org.taskforce.episample.filter

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.taskforce.episample.db.StudyRoomDatabase
import org.taskforce.episample.db.config.ConfigDao
import org.taskforce.episample.db.config.ResolvedConfigDao
import org.taskforce.episample.db.config.StudyDao
import org.taskforce.episample.db.config.customfield.*
import org.taskforce.episample.db.config.customfield.metadata.NumberMetadata
import org.taskforce.episample.db.filter.Filter
import org.taskforce.episample.db.filter.Rule
import org.taskforce.episample.db.filter.RuleDao
import org.taskforce.episample.db.filter.RuleSet
import org.taskforce.episample.db.filter.integers.IntRuleFactory
import org.taskforce.episample.db.utils.CommonSetup
import java.io.IOException
import java.util.*

//
//@RunWith(AndroidJUnit4::class)
//class RulePersistenceTest {
//    private var configDao: ConfigDao? = null
//    private var resolvedConfigDao: ResolvedConfigDao? = null
//    private var customFieldDao: CustomFieldDao? = null
//    private var customFieldValueDao: CustomFieldValueDao? = null
//    private var ruleDao: RuleDao? = null
//    private var db: StudyRoomDatabase? = null
//    lateinit var studyId: String
//    private var studyDao: StudyDao? = null
//    lateinit var customField: CustomField
//    lateinit var greaterThanRule: Rule
//    private val configId = UUID.randomUUID().toString()
//    private val ruleSet = RuleSet("My first rule set", configId, true)
//
//    @Before
//    fun createEnumerations() {
//        val context = InstrumentationRegistry.getTargetContext()
//        db = Room.inMemoryDatabaseBuilder(context, StudyRoomDatabase::class.java).build()
//        configDao = db?.configDao()
//        ruleDao = db?.ruleDao()
//        studyDao = db?.studyDao()
//        resolvedConfigDao = db?.resolvedConfigDao()
//        customFieldDao = db?.customFieldDao()
//        customFieldValueDao = db?.customFieldValueDao()
//
//        CommonSetup.setupConfig(configDao!!, configId)
//
//        val numberMetadata = NumberMetadata(true)
//        customField = CommonSetup.makeCustomField("name",
//                CustomFieldType.NUMBER,
//                numberMetadata,
//                configId
//        )
//
//        configDao?.insert(customField)
//
//        greaterThanRule = IntRuleFactory.makeRule(IntRuleFactory.Rules.GREATER_THAN, customField, 5)
//        configDao?.insert(ruleSet)
//
//        ruleDao?.insert(greaterThanRule.toRecord(ruleSet.id))
//
//        val resolvedConfig = resolvedConfigDao!!.getConfigSync(configId)
//        studyId = UUID.randomUUID().toString()
//
//        studyDao?.insert(studyId, "Study 1", "Study Password", resolvedConfig)
//
//        for (i in 1..10) {
//            val enumerationId = UUID.randomUUID().toString()
//            val insertEnumeration = CommonSetup.makeEnumeration(studyId, enumerationId)
//            studyDao?.insert(insertEnumeration)
//
//            val intValue = org.taskforce.episample.db.config.customfield.value.IntValue(i)
//
//            val insertFieldValue = CustomFieldValue(intValue,
//                    CustomFieldType.NUMBER,
//                    enumerationId,
//                    customField.id)
//
//            customFieldValueDao?.insert(insertFieldValue)
//        }
//    }
//
//    @After
//    @Throws(IOException::class)
//    fun closeDb() {
//        db?.close()
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testInsertRuleSetSucceeded() {
//        val retrievedRuleSet = configDao?.getRuleSetsByConfigSync(configId)
//        val retrievedGreaterThanRule = ruleDao?.getAllRulesByRuleSet(ruleSet.id)?.first()?.rule!!
//
//        Assert.assertEquals(retrievedRuleSet?.size, 1)
//        Assert.assertEquals(retrievedRuleSet?.first()?.name, ruleSet.name)
//        Assert.assertEquals(retrievedRuleSet?.first()?.id, ruleSet.id)
//
//        Assert.assertEquals(retrievedGreaterThanRule.forField.id, greaterThanRule.forField.id)
//
//        val filterFromRetrievedRule = Filter(listOf(retrievedGreaterThanRule))
//        val filterFromRule = Filter(listOf(greaterThanRule))
//
//        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)
//
//        val filteredEnumerationsFromRetrievedRule = resolvedEnumerations?.let {
//            filterFromRetrievedRule.filterAny(it)
//        }
//
//        val filteredEnumerationsFromRule = resolvedEnumerations?.let {
//            filterFromRule.filterAny(it)
//        }
//
//        Assert.assertEquals(filteredEnumerationsFromRetrievedRule?.size, filteredEnumerationsFromRule?.size)
//        filteredEnumerationsFromRetrievedRule?.zip(filteredEnumerationsFromRule!!)?.forEach {
//            Assert.assertEquals(it.first.id, it.second.id)
//        }
//
//    }
//}
