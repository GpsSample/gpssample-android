package org.taskforce.episample

import android.app.Application
import dagger.Component
import org.taskforce.episample.injection.*
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
