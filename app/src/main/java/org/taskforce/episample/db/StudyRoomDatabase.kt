package org.taskforce.episample.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.os.AsyncTask
import org.taskforce.episample.db.collect.*
import org.taskforce.episample.db.config.*
import org.taskforce.episample.db.config.customfield.CustomField
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.config.customfield.CustomFieldValueDao
import org.taskforce.episample.db.config.landmark.CustomLandmarkType
import org.taskforce.episample.db.filter.RuleDao
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.RuleSet
import org.taskforce.episample.db.navigation.NavigationDao
import org.taskforce.episample.db.navigation.NavigationItem
import org.taskforce.episample.db.navigation.NavigationPlan

@Database(version = 1,
        entities = [
            AdminSettings::class,
            Config::class,
            CustomField::class,
            CustomFieldValue::class,
            CustomLandmarkType::class,
            DisplaySettings::class,
            Enumeration::class,
            EnumerationSubject::class,
            GpsBreadcrumb::class,
            Landmark::class,
            NavigationPlan::class,
            NavigationItem::class,
            RuleSet::class,
            RuleRecord::class,
            Study::class,
            UserSettings::class
        ])
abstract class StudyRoomDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao
    abstract fun studyDao(): StudyDao
    abstract fun resolvedConfigDao(): ResolvedConfigDao
    abstract fun breadcrumbDao(): GpsBreakcrumbDao
    abstract fun resolvedStudyDao(): ResolvedStudyDao
    abstract fun customFieldDao(): CustomFieldDao
    abstract fun resolvedEnumerationDao(): ResolvedEnumerationDao
    abstract fun ruleDao(): RuleDao
    abstract fun customFieldValueDao(): CustomFieldValueDao
    abstract fun navigationDao(): NavigationDao

    private class PopulateDbAsync(db: StudyRoomDatabase) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            return null
        }
    }

    companion object {
        private var INSTANCE: StudyRoomDatabase? = null
//        private var BACKUP: StudyRoomDatabase? = null

        fun reloadDatabaseInstance(context: Context): StudyRoomDatabase {
            INSTANCE = null
            return getDatabase(context)
        }
        fun getDatabase(context: Context): StudyRoomDatabase {
            if (INSTANCE == null) {
                synchronized(ConfigRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                StudyRoomDatabase::class.java, "study_database")
                                .addCallback(roomDatabaseCallback)
                                .fallbackToDestructiveMigration()
                                .build()
                    }
                }
            }
            return INSTANCE!!
        }

//        fun getBackupInstance(context: Context): StudyRoomDatabase {
//            if (BACKUP == null) {
//                synchronized(ConfigRoomDatabase::class.java) {
//                    if (BACKUP == null) {
//                        BACKUP = Room.databaseBuilder(context.applicationContext,
//                                StudyRoomDatabase::class.java, "study_database_backup")
//                                .addCallback(roomDatabaseCallback)
//                                .fallbackToDestructiveMigration()
//                                .build()
//                    }
//                }
//            }
//            return BACKUP!!
//        }

        private val roomDatabaseCallback = object : RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                PopulateDbAsync(INSTANCE!!).execute()
            }
        }

    }
}