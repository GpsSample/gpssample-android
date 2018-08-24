package org.taskforce.episample.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.os.AsyncTask
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreadcrumb
import org.taskforce.episample.db.collect.GpsBreakcrumbDao
import org.taskforce.episample.db.collect.ResolvedEnumerationDao
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.customfield.CustomFieldValue


@Database(version = 22,
        entities = [
            (AdminSettings::class),
            (Config::class),
            (CustomField::class),
            (CustomFieldValue::class),
            (Enumeration::class),
            (EnumerationSubject::class),
            (GpsBreadcrumb::class),
            (Study::class)
        ])

abstract class ConfigRoomDatabase : RoomDatabase() {

    abstract fun configDao(): ConfigDao
    abstract fun studyDao(): StudyDao
    abstract fun resolvedConfigDao(): ResolvedConfigDao
    abstract fun breadcrumbDao(): GpsBreakcrumbDao
    abstract fun resolvedStudyDao(): ResolvedStudyDao
    abstract fun customFieldDao(): CustomFieldDao
    abstract fun resolvedEnumerationDao(): ResolvedEnumerationDao

    private class PopulateDbAsync(db: ConfigRoomDatabase) : AsyncTask<Void, Void, Void>() {

        private val configDao: ConfigDao = db.configDao()
        private val studyDao: StudyDao = db.studyDao()

        override fun doInBackground(vararg params: Void): Void? {
//            configDao.deleteAll()
//            studyDao.deleteAll()

//            val id = studyDao.insert(Study("Study Name", Date()))
//            val config = Config("Config Name", Date())
//            config.studyId = id
//            configDao.insert(config)

//            configDao.insert(Config("Config Name", Date()))

            return null
        }
    }

    companion object {
        private var INSTANCE: ConfigRoomDatabase? = null

        fun getDatabase(context: Context): ConfigRoomDatabase {
            if (INSTANCE == null) {
                synchronized(ConfigRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                ConfigRoomDatabase::class.java, "config_database")
                                .addCallback(roomDatabaseCallback)
                                .fallbackToDestructiveMigration()
                                .build()
                    }
                }
            }
            return INSTANCE!!
        }

        private val roomDatabaseCallback = object : RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                PopulateDbAsync(INSTANCE!!).execute()
            }
        }

    }
}

