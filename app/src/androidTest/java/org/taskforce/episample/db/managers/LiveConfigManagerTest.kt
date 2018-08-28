package org.taskforce.episample.db.managers

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.taskforce.episample.config.base.Config
import org.taskforce.episample.core.BuiltInLandmark
import org.taskforce.episample.core.interfaces.ConfigManager
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.ConfigRoomDatabase
import org.taskforce.episample.db.utils.CommonSetup
import org.taskforce.episample.db.utils.blockingObserve
import org.taskforce.episample.managers.LiveConfigManager
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class LiveConfigManagerTest {

    private var configRepository: ConfigRepository? = null
    private var configManager: ConfigManager? = null
    private var db: ConfigRoomDatabase? = null

    private val customLandmarkSource = listOf(
            Config.CustomLandmarkTypeInput("Name 1", "Location 1"),
            Config.CustomLandmarkTypeInput("Name 2", "Location 2")
    )

    val context: Context
        get() = InstrumentationRegistry.getTargetContext()

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(context, ConfigRoomDatabase::class.java).build()
        configRepository = ConfigRepository(context.applicationContext as Application, db)

        val syncObject = Object()

        CommonSetup.setupConfigAndStudy(configRepository!!,
                customLandmarkTypes = customLandmarkSource,
                callback = { configId, studyId ->
                    configManager = LiveConfigManager(configRepository!!, configId)

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
    fun getLandmarkTypes() {
        val config = configManager!!.getConfig(context).blockingObserve()
        assertNotNull(config)

        assertEquals(BuiltInLandmark.getLandmarkTypes(context).size + customLandmarkSource.size, config!!.landmarkTypes.size)
    }
}

