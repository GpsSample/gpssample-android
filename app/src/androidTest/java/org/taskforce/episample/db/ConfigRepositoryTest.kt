package org.taskforce.episample.db

import android.app.Application
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.config.fields.CustomFieldTypeConstants
import org.taskforce.episample.config.settings.admin.AdminSettings
import org.taskforce.episample.config.settings.display.DisplaySettings
import org.taskforce.episample.config.settings.user.UserSettings
import org.taskforce.episample.core.interfaces.LiveEnumerationSubject
import org.taskforce.episample.db.config.customfield.CustomFieldType
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ConfigRepositoryTest {

    private var configRepository: ConfigRepository? = null
    private var studyRepository: StudyRepository? = null
    private var studyDb: StudyRoomDatabase? = null
    private var db: ConfigRoomDatabase? = null

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(context, ConfigRoomDatabase::class.java).build()
        studyDb = Room.inMemoryDatabaseBuilder(context, StudyRoomDatabase::class.java).build()
        configRepository = ConfigRepository(context.applicationContext as Application, db, studyDb)
        studyRepository = StudyRepository(context.applicationContext as Application, studyDb)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
        configRepository = null
    }

    @Test
    @Throws(Exception::class)
    fun writeConfigRelationsAndReadInResolvedConfig() {
        val syncObject = Object()

        val configName = "Config 1"
        val enumerationSingular = "Person"
        val enumerationPlural = "People"
        val enumerationLabel = "Name of Person"
        val adminPassword = "anypassword"
        val gpsMinimumPrecision = 40.0
        val gpsPreferredPrecision = 20.0
        val configBuilder = org.taskforce.episample.config.base.Config(name = configName)
        configBuilder.adminSettings = AdminSettings(adminPassword)
        configBuilder.enumerationSubject = LiveEnumerationSubject(enumerationSingular, enumerationPlural, enumerationLabel)
        configBuilder.userSettings = UserSettings(gpsMinimumPrecision, gpsPreferredPrecision,
                false, null, false, false, null, false, false, null)

        configRepository?.insertConfigFromBuildManager(configBuilder) {
            val resolvedConfig = configRepository!!.getResolvedConfigSync(it)
            assertEquals(configName, resolvedConfig.name)
            assertEquals(adminPassword, resolvedConfig.adminSettings.password)
            assertEquals(enumerationSingular, resolvedConfig.enumerationSubject.singular)
            assertEquals(enumerationPlural, resolvedConfig.enumerationSubject.plural)
            assertEquals(enumerationLabel, resolvedConfig.enumerationSubject.primaryLabel)
            assertEquals(gpsMinimumPrecision, resolvedConfig.userSettings.gpsMinimumPrecision)
            assertEquals(gpsPreferredPrecision, resolvedConfig.userSettings.gpsPreferredPrecision)

            synchronized(syncObject) {
                syncObject.notify()
            }
        }

        synchronized(syncObject) {
            syncObject.wait()
        }
    }

    @Test
    @Throws(Exception::class)
    fun writeCompleteConfigAddStudyAndReadInResolvedConfig() {
        val syncObject = Object()

        val configName = "Config 1"
        val enumerationSingular = "Person"
        val enumerationPlural = "People"
        val enumerationLabel = "Name of Person"
        val adminPassword = "anypassword"
        val gpsMinimumPrecision = 40.0
        val gpsPreferredPrecision = 20.0
        val isMetricDate = true
        val is24HourTime = true

        val configBuilder = org.taskforce.episample.config.base.Config(name = configName)
        configBuilder.adminSettings = AdminSettings(adminPassword)
        configBuilder.enumerationSubject = LiveEnumerationSubject(enumerationSingular, enumerationPlural, enumerationLabel)
        configBuilder.customFields = listOf(
                CustomField(true, true, true, true, true, "Custom Number", CustomFieldType.NUMBER,
                        mapOf(CustomFieldTypeConstants.INTEGER_ONLY to true))
        )
        configBuilder.userSettings = UserSettings(gpsMinimumPrecision, gpsPreferredPrecision,
                false, null, false, false, null, false, false, null)
        configBuilder.displaySettings = DisplaySettings(isMetricDate, is24HourTime)

        configRepository?.insertConfigFromBuildManager(configBuilder) {
            val resolvedConfig = configRepository!!.getResolvedConfigSync(it)
            assertEquals(configName, resolvedConfig.name)
            assertEquals(adminPassword, resolvedConfig.adminSettings.password)
            assertEquals(enumerationSingular, resolvedConfig.enumerationSubject.singular)
            assertEquals(enumerationSingular, resolvedConfig.enumerationSubject.singular)
            assertEquals(enumerationPlural, resolvedConfig.enumerationSubject.plural)
            assertEquals(enumerationLabel, resolvedConfig.enumerationSubject.primaryLabel)
            assertEquals(gpsMinimumPrecision, resolvedConfig.userSettings.gpsMinimumPrecision)
            assertEquals(gpsPreferredPrecision, resolvedConfig.userSettings.gpsPreferredPrecision)
            assertEquals(isMetricDate, resolvedConfig.displaySettings.isMetricDate)
            assertEquals(is24HourTime, resolvedConfig.displaySettings.is24HourTime)
            assertEquals(true, resolvedConfig.customFields[0].isRequired)

            configRepository?.insertStudy(resolvedConfig, "Study Name", "Study Password") { configId, _ ->

                val resolvedStudyConfig = studyRepository!!.getResolvedConfigSync(configId)

                assertEquals(configName, resolvedStudyConfig.name)
                assertEquals(adminPassword, resolvedStudyConfig.adminSettings.password)
                assertEquals(enumerationSingular, resolvedStudyConfig.enumerationSubject.singular)
                assertEquals(enumerationSingular, resolvedStudyConfig.enumerationSubject.singular)
                assertEquals(enumerationPlural, resolvedStudyConfig.enumerationSubject.plural)
                assertEquals(enumerationLabel, resolvedStudyConfig.enumerationSubject.primaryLabel)

                assertEquals(gpsMinimumPrecision, resolvedStudyConfig.userSettings.gpsMinimumPrecision)
                assertEquals(gpsPreferredPrecision, resolvedStudyConfig.userSettings.gpsPreferredPrecision)
                assertEquals(isMetricDate, resolvedStudyConfig.displaySettings.isMetricDate)
                assertEquals(is24HourTime, resolvedStudyConfig.displaySettings.is24HourTime)

                synchronized(syncObject) {
                    syncObject.notify()
                }
            }
        }

        synchronized(syncObject) {
            syncObject.wait()
        }
    }
}