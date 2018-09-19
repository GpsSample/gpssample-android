package org.taskforce.episample.db

import android.app.Application
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import junit.framework.Assert
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.config.fields.CustomFieldTypeConstants
import org.taskforce.episample.core.interfaces.LiveCustomFieldValue
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.value.IntValue
import org.taskforce.episample.db.utils.CommonSetup
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class StudyRepositoryTest {

    private var configRepository: ConfigRepository? = null
    private var studyRepository: StudyRepository? = null
    private var configDb: ConfigRoomDatabase? = null
    private var studyDb: StudyRoomDatabase? = null

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        configDb = Room.inMemoryDatabaseBuilder(context, ConfigRoomDatabase::class.java).build()
        studyDb = Room.inMemoryDatabaseBuilder(context, StudyRoomDatabase::class.java).build()
        configRepository = ConfigRepository(context.applicationContext as Application, configDb, studyDb)

        studyRepository = StudyRepository(context.applicationContext as Application, studyDb)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        configDb?.close()
        studyDb?.close()
        configRepository = null
        studyRepository = null
    }

    @Test
    @Throws(Exception::class)
    fun insertReadUpdateEnumerations() {
        val syncObject = Object()
        val expectedLat = 121.0
        val expectedLng = 133.1231
        val expectedNumberFieldValue = 20

        val integerCustomField = CustomField(true, true, true, true, true, "Custom Number", CustomFieldType.NUMBER,
                mapOf(CustomFieldTypeConstants.INTEGER_ONLY to true))

        CommonSetup.setupConfigAndStudy(configRepository!!, studyRepository!!, customFields = listOf(integerCustomField)) { configId, studyId ->

            val resolvedConfig = studyRepository!!.getResolvedConfigSync(configId)
            val enumeration = Enumeration("Jesse",
                    expectedLat, expectedLng, null, true, false, 25.12, studyId, null, null)

            val customFieldValues = listOf(
                    LiveCustomFieldValue(
                            IntValue(expectedNumberFieldValue),
                            CustomFieldType.NUMBER,
                            resolvedConfig.customFields.first().id
                    )
            )

            studyRepository!!.insertEnumerationItem(enumeration, customFieldValues, {

                val enumerations = studyRepository!!.getResolvedEnumerationsSync(studyId)
                val dbCustomFieldValues = enumerations.first().customFieldValues

                Assert.assertEquals(expectedLat, enumerations.first().lat)
                Assert.assertEquals(expectedLng, enumerations.first().lng)
                val intValue = enumerations.first().customFieldValues.first().value as IntValue
                Assert.assertEquals(expectedNumberFieldValue, intValue.intValue)

                enumeration.collectorName = "New collector name"
                (dbCustomFieldValues.first().value as IntValue).intValue = 10

                studyRepository!!.updateEnumerationItem(enumeration, dbCustomFieldValues, {

                    val updatedEnumeration = studyRepository!!.getResolvedEnumerationsSync(studyId)
                    val intValue = updatedEnumeration.first().customFieldValues[0].value as IntValue
                    Assert.assertEquals(10, intValue.intValue)
                    Assert.assertEquals("New collector name", updatedEnumeration.first().collectorName)
                    synchronized(syncObject) {
                        syncObject.notify()
                    }
                })
            })
        }

        synchronized(syncObject) {
            syncObject.wait()
        }
    }
}