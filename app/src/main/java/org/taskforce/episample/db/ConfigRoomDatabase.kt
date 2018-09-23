package org.taskforce.episample.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.os.AsyncTask
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.landmark.CustomLandmarkType
import org.taskforce.episample.db.filter.RuleDao
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.RuleSet
import org.taskforce.episample.db.sampling.subsets.Subset
import retrofit2.http.HEAD

@Database(version = 27,
        entities = [
            AdminSettings::class,
            Config::class,
            CustomField::class,
            CustomLandmarkType::class,
            DisplaySettings::class,
            EnumerationSubject::class,
            RuleSet::class,
            RuleRecord::class,
            Study::class,
            Subset::class,
            UserSettings::class
        ])

abstract class ConfigRoomDatabase : RoomDatabase() {

    abstract fun configDao(): ConfigDao
    abstract fun resolvedConfigDao(): ResolvedConfigDao
    abstract fun customFieldDao(): CustomFieldDao
    abstract fun ruleDao(): RuleDao

    private class PopulateDbAsync(db: ConfigRoomDatabase) : AsyncTask<Void, Void, Void>() {

        private val configDao: ConfigDao = db.configDao()

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