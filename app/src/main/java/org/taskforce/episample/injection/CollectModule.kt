package org.taskforce.episample.injection

import android.app.Application
import dagger.Module
import dagger.Provides
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.ConfigManager
import org.taskforce.episample.db.config.IntegrationCollectManager
import org.taskforce.episample.managers.LiveConfigManager

@Module
class CollectModule(val application: Application,
                    val configId: String,
                    val studyId: String) {
    @Provides
    fun providesCollectManager(): CollectManager = IntegrationCollectManager(application, configId, studyId)

    @Provides
    fun providesConfigManager(): ConfigManager = LiveConfigManager(application, configId)
}