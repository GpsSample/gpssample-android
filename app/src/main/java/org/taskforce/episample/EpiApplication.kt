package org.taskforce.episample

import android.app.Application
import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Relation
import android.arch.persistence.room.TypeConverters
import dagger.Component
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.StudyRoomDatabase
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreadcrumb
import org.taskforce.episample.db.converter.DateConverter
import org.taskforce.episample.injection.*
import org.taskforce.episample.sync.core.StudyDatabaseFilesChangedMessage
import java.util.*
import javax.inject.Singleton


open class EpiApplication : Application() {

    @Singleton
    @Component(modules = [EpiModule::class, ConfigStorageModule::class, StudyStorageModule::class])
    interface AppComponent : EpiComponent

    lateinit var component: EpiComponent
    var collectComponent: CollectComponent? = null

    override fun onCreate() {
        super.onCreate()

        component = DaggerEpiApplication_AppComponent.builder()
                .epiModule(EpiModule(this))
                .configStorageModule(ConfigStorageModule())
                .studyStorageModule(StudyStorageModule())
                .build()

    }

    fun createCollectComponent(collectModule: CollectModule): CollectComponent {
        collectComponent = component.plus(collectModule)
        return collectComponent!!
    }

    fun clearCollectComponent() {
        collectComponent = null
    }
}

