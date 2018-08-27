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
import org.taskforce.episample.config.settings.user.UserSettings
import org.taskforce.episample.db.config.customfield.CustomFieldType
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ConfigRepositoryTest {

    private var configRepository: ConfigRepository? = null
    private var db: ConfigRoomDatabase? = null

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(context, ConfigRoomDatabase::class.java).build()
        configRepository = ConfigRepository(context.applicationContext as Application, db)
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
        val adminPassword = "anypassword"
        val gpsMinimumPrecision = 40.0
        val gpsPreferredPrecision = 20.0
        val configBuilder = org.taskforce.episample.config.base.Config(name = configName)
        configBuilder.adminSettings = AdminSettings(adminPassword)
        configBuilder.enumerationSubject = enumerationSingular
        configBuilder.userSettings = UserSettings(gpsMinimumPrecision, gpsPreferredPrecision,
                false, null, false, false, null, false, false, null)

        configRepository?.insertConfigFromBuildManager(configBuilder) {
            val resolvedConfigs = configRepository!!.getResolvedConfigSync(it)
            assertEquals(1, resolvedConfigs.size)
            assertEquals(configName, resolvedConfigs[0].name)
            assertEquals(adminPassword, resolvedConfigs[0].adminSettings.password)
            assertEquals(enumerationSingular, resolvedConfigs[0].enumerationSubject.singular)
            assertEquals(gpsMinimumPrecision, resolvedConfigs[0].userSettings.gpsMinimumPrecision)
            assertEquals(gpsPreferredPrecision, resolvedConfigs[0].userSettings.gpsPreferredPrecision)

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
        val adminPassword = "anypassword"
        val gpsMinimumPrecision = 40.0
        val gpsPreferredPrecision = 20.0

        val configBuilder = org.taskforce.episample.config.base.Config(name = configName)
        configBuilder.adminSettings = AdminSettings(adminPassword)
        configBuilder.enumerationSubject = enumerationSingular
        configBuilder.customFields = listOf(
                CustomField(true, true, true, true, true, "Custom Number", CustomFieldType.NUMBER,
                        mapOf(CustomFieldTypeConstants.INTEGER_ONLY to true))
        )
        configBuilder.userSettings = UserSettings(gpsMinimumPrecision, gpsPreferredPrecision,
                false, null, false, false, null, false, false, null)

        configRepository?.insertConfigFromBuildManager(configBuilder) {
            val resolvedConfigs = configRepository!!.getResolvedConfigSync(it)
            assertEquals(1, resolvedConfigs.size)
            assertEquals(configName, resolvedConfigs[0].name)
            assertEquals(adminPassword, resolvedConfigs[0].adminSettings.password)
            assertEquals(enumerationSingular, resolvedConfigs[0].enumerationSubject.singular)
            assertEquals(gpsMinimumPrecision, resolvedConfigs[0].userSettings.gpsMinimumPrecision)
            assertEquals(gpsPreferredPrecision, resolvedConfigs[0].userSettings.gpsPreferredPrecision)
            val config = configRepository!!.getConfigSync(it)

            configRepository?.insertStudy(config, "Study Name", "Study Password") { configId, _ ->

                val resolvedConfigsStudyConfig = configRepository!!.getResolvedConfigSync(configId)
                val resolvedConfig = resolvedConfigsStudyConfig.first()
                assertEquals(1, resolvedConfigsStudyConfig.size)
                assertEquals(configName, resolvedConfig.name)
                assertEquals(adminPassword, resolvedConfig.adminSettings.password)
                assertEquals(enumerationSingular, resolvedConfig.enumerationSubject.singular)
                assertEquals(gpsMinimumPrecision, resolvedConfig.userSettings.gpsMinimumPrecision)
                assertEquals(gpsPreferredPrecision, resolvedConfig.userSettings.gpsPreferredPrecision)

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