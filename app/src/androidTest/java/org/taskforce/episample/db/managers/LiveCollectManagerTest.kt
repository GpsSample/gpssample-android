package org.taskforce.episample.db.managers

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.taskforce.episample.config.base.Config
import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.core.BuiltInLandmark
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.ConfigRoomDatabase
import org.taskforce.episample.db.config.LiveCollectManager
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.value.TextValue
import org.taskforce.episample.db.utils.CommonSetup
import org.taskforce.episample.db.utils.blockingObserve
import org.taskforce.episample.managers.LiveConfigManager
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class LiveCollectManagerTest {

    private var configRepository: ConfigRepository? = null
    private var configManager: ConfigManager? = null
    private var collectManager: CollectManager? = null
    private var db: ConfigRoomDatabase? = null

    private val customLandmarkSource = listOf(
            Config.CustomLandmarkTypeInput("Name 1", "Location 1"),
            Config.CustomLandmarkTypeInput("Name 2", "Location 2")
    )

    private val customFieldSource = listOf(
            CustomField(false, false, false, false, false, "CUstom Field",
                    CustomFieldType.TEXT, mapOf())
    )

    val context: Context
        get() = InstrumentationRegistry.getTargetContext()

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(context, ConfigRoomDatabase::class.java).build()
        configRepository = ConfigRepository(context.applicationContext as Application, db)


        val syncObject = Object()

        CommonSetup.setupConfigAndStudy(configRepository!!,
                customFields = customFieldSource,
                customLandmarkTypes = customLandmarkSource,
                callback = { configId, studyId ->
                    configManager = LiveConfigManager(configRepository!!, configId)
                    collectManager = LiveCollectManager(context.applicationContext as Application,
                            configManager!!, configRepository!!, LiveUserSession("Jesse", false, configId, studyId))
                    synchronized(syncObject) {
                        syncObject.notify()
                    }
                })
        synchronized(syncObject) {
            syncObject.wait()
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
        configRepository = null
        configManager = null
    }

    @Test
    @Throws(Exception::class)
    fun insertAndReadLandmarks() {
        val syncObject = Object()

        collectManager!!.addLandmark(LiveLandmark("B41 Stop 1",
                LiveLandmarkType("Bus-stop", "iconLocation", LandmarkTypeMetadata.BuiltInLandmark(BuiltInLandmark.BUS_STOP)),
                "Note",
                null,
                LatLng(121.0, 14.0),
                23.2
        ), {
            var landmarks = collectManager!!.getLandmarks().blockingObserve()
            assertEquals(1, landmarks?.size)
            synchronized(syncObject) {
                syncObject.notify()
            }
        })

        synchronized(syncObject) {
            syncObject.wait()
        }
    }

    @Test
    @Throws(Exception::class)
    fun insertAndReadEnumerations() {
        val syncObject = Object()

        val config = configManager!!.getConfig(context).blockingObserve()!!
        val customFieldsValues = config.customFields.map {
            LiveCustomFieldValue(TextValue("Filled out value"), CustomFieldType.TEXT, it.id)
        }

        collectManager!!.addEnumerationItem(LiveEnumeration(null, false, false, "title", "note", LatLng(20.1, 20.2), 25.6,
                "TODO", customFieldValues = customFieldsValues)) {

            val enumerations = collectManager!!.getEnumerations().blockingObserve()
            assertEquals(1, enumerations?.size)
            assertEquals("Filled out value", (enumerations!!.first().customFieldValues.first().value as TextValue).text)
            synchronized(syncObject) {
                syncObject.notify()
            }
        }

        synchronized(syncObject) {
            syncObject.wait()
        }
    }
}