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
import org.taskforce.episample.db.collect.ResolvedEnumerationDao
import org.taskforce.episample.db.config.ConfigDao
import org.taskforce.episample.db.config.ResolvedConfigDao
import org.taskforce.episample.db.config.StudyDao
import org.taskforce.episample.db.config.customfield.*
import org.taskforce.episample.db.config.customfield.metadata.EmptyMetadata
import org.taskforce.episample.db.config.customfield.value.BooleanValue
import org.taskforce.episample.db.config.customfield.value.TextValue
import org.taskforce.episample.db.filter.Filter
import org.taskforce.episample.db.filter.checkbox.BooleanRuleFactory
import org.taskforce.episample.db.filter.text.TextRuleFactory
import org.taskforce.episample.db.utils.CommonSetup
import java.io.IOException
import java.util.*


@RunWith(AndroidJUnit4::class)
class AnyFilterEnumerationTest {

    private val filterOddNumberValue = false
    private val textFieldValue = "Find me"

    private var configDao: ConfigDao? = null
    private var resolvedConfigDao: ResolvedConfigDao? = null
    private var studyDao: StudyDao? = null
    private var customFieldDao: CustomFieldDao? = null
    private var customFieldValueDao: CustomFieldValueDao? = null
    private var resolvedEnumerationDao: ResolvedEnumerationDao? = null
    private var db: StudyRoomDatabase? = null
    lateinit var studyId: String
    lateinit var checkboxField: CustomField
    lateinit var textField: CustomField

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
        CommonSetup.setupConfig(configDao!!, insertConfigId)

        val resolvedConfig = resolvedConfigDao!!.getConfigSync(insertConfigId)
        studyId = UUID.randomUUID().toString()

        val studyConfigId = studyDao!!.insert(studyId, "Study 1", "Study Password", resolvedConfig)

        checkboxField = CommonSetup.makeCustomField("isOddNumber",
                CustomFieldType.CHECKBOX,
                EmptyMetadata(),
                studyConfigId
        )

        textField = CommonSetup.makeCustomField("findMeEvery5th", CustomFieldType.TEXT, EmptyMetadata(), insertConfigId)

        configDao?.insert(checkboxField)
        configDao?.insert(textField)

        for (i in 1..10) {
            val enumerationId = UUID.randomUUID().toString()
            val insertEnumeration = CommonSetup.makeEnumeration(studyId, enumerationId)
            studyDao?.insert(insertEnumeration)

            val doubleValue = BooleanValue(i % 2 == 1)
            val textValue = TextValue(if (i % 5 == 0) textFieldValue else "Dont find me")

            val insertFieldValue = CustomFieldValue(doubleValue,
                    CustomFieldType.CHECKBOX,
                    enumerationId,
                    checkboxField.id)

            customFieldValueDao?.insert(insertFieldValue)

            val insertTextFieldValue = CustomFieldValue(textValue,
                    CustomFieldType.TEXT,
                    enumerationId,
                    textField.id)

            customFieldValueDao?.insert(insertTextFieldValue)

        }
    }

    /*
        BEFORE MODIFYING THE FOLLOWING TESTS, UNDERSTAND THESE EXPECTATIONS
        we have a set of indices from [1...10] which is looped through creating 10 enumerations. There are 2 different custom fields added to
        this particular configuration: a checkbox field and a text field. These tests are for confirming the interactions of the rules work as expected.

        The text field value is set to "Find me" if the index % 5 == 0. That is, for indices 5 and 10. The checkbox field is set to index % 2 == 1,
        by which I mean it is set to true if the number is odd.

        Fig. 1 -- a chart of the values by index
        BF == checkbox field value, TF == text field value, X == Dont find me, F == Find me
        Index   1        2        3        4        5        6        7        8        9        10
        BF     true    false    true      false    true    false     true    false     true     false
        TF      X        X        X        X        F        X        X        X        X        F
     */

    /*
        filterAny() is testing the filterAny function on Filter which should be used when you want a subset where any of the rules apply. IE
        all numbers that are odd **OR** have the value "Find me" in their text field. This is 6 enumerations. All the odd ones (5) and the ones with
        "Find me" as text which are at index 5 and 10. 5 is already there, so the one at 10 is the only additional one for a count of 6 filtered enumerations
     */
    @Test
    @Throws(Exception::class)
    fun filterAny() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filter = Filter(
                listOf(
                        BooleanRuleFactory.makeRule(BooleanRuleFactory.Rules.IS_EQUAL_TO, checkboxField, filterOddNumberValue),
                        TextRuleFactory.makeRule(TextRuleFactory.Rules.IS_EQUAL_TO, textField, textFieldValue)
                )
        )

        val filteredEnumerations = resolvedEnumerations?.let {
            filter.filterAny(it)
        }

        Assert.assertEquals(6, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            val valueForCheckBoxField = it.customFieldValues.find { it.customFieldId == checkboxField.id }?.value as BooleanValue?
            val valueForTextField = it.customFieldValues.find { it.customFieldId == textField.id }?.value as TextValue?
            Assert.assertTrue(valueForCheckBoxField?.boolValue == filterOddNumberValue || valueForTextField?.text == textFieldValue)
        }
    }

    /*
    filterAll() is testing the filterAll function on Filter which should be used when you want a subset where any of the rules apply. IE
    all numbers that are odd **AND** have the value "Find me" in their text field. This is 1 enumeration only. All the odd ones (5) and the ones with
    "Find me" as text which are at index 5 and 10. The enumerations at index 5 is the only one with an odd index AND the text of "Find me"
    */
    @Test
    @Throws(Exception::class)
    fun filterAll() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filter = Filter(
                listOf(
                        BooleanRuleFactory.makeRule(BooleanRuleFactory.Rules.IS_EQUAL_TO, checkboxField, filterOddNumberValue),
                        TextRuleFactory.makeRule(TextRuleFactory.Rules.IS_EQUAL_TO, textField, textFieldValue)
                )
        )

        val filteredEnumerations = resolvedEnumerations?.let {
            filter.filterAll(it)
        }

        Assert.assertEquals(1, filteredEnumerations?.size)
        filteredEnumerations?.forEach {
            val valueForCheckBoxField = it.customFieldValues.find { it.customFieldId == checkboxField.id }?.value as BooleanValue?
            val valueForTextField = it.customFieldValues.find { it.customFieldId == textField.id }?.value as TextValue?
            Assert.assertTrue(valueForCheckBoxField?.boolValue == filterOddNumberValue && valueForTextField?.text == textFieldValue)
        }
    }

    /*
        Tentatively no grouping will have filtering without any rules. This test ensures that filterAny and filterAll resolve all the enumerations
        when there are no rules
     */
    @Test
    @Throws(Exception::class)
    fun filterWithoutRules() {
        val resolvedEnumerations = studyDao?.getResolvedEnumerationsSync(studyId)

        val filter = Filter(
                listOf()
        )

        val filteredAllEnumerations = resolvedEnumerations?.let {
            filter.filterAll(it)
        }

        val filteredAnyEnumerations = resolvedEnumerations?.let {
            filter.filterAll(it)
        }

        Assert.assertEquals(resolvedEnumerations?.size, filteredAllEnumerations?.size)
        Assert.assertEquals(resolvedEnumerations?.size, filteredAnyEnumerations?.size)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
    }
}