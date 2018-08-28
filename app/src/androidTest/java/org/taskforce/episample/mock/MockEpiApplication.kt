package org.taskforce.episample.mock

import com.squareup.leakcanary.LeakCanary
import dagger.Component
import org.mockito.Mockito
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.config.base.ConfigStorage
import org.taskforce.episample.injection.EpiComponent
import org.taskforce.episample.injection.EpiModule
import org.taskforce.episample.injection.StudyStorageModule
import javax.inject.Singleton

class MockEpiApplication : EpiApplication() {

    @Singleton
    @Component(modules = [EpiModule::class, MockConfigStorageModule::class, StudyStorageModule::class])
    interface TestComponent : EpiComponent

    override fun onCreate() {
        super.onCreate()

        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this)

            component = DaggerMockEpiApplication_TestComponent.builder()
                    .epiModule(EpiModule(applicationContext))
                    .mockConfigStorageModule(MockConfigStorageModule(Mockito.mock(ConfigStorage::class.java)))
                    .studyStorageModule(StudyStorageModule())
                    .build()
        }
    }
}