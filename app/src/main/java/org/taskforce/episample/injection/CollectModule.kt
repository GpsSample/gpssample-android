package org.taskforce.episample.injection

import android.app.Application
import dagger.Module
import dagger.Provides
import org.taskforce.episample.core.interfaces.CollectManager
import org.taskforce.episample.core.interfaces.Config
import org.taskforce.episample.core.interfaces.ConfigManager
import org.taskforce.episample.core.interfaces.UserSession
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.ConfigRoomDatabase
import org.taskforce.episample.db.config.LiveCollectManager
import org.taskforce.episample.managers.LiveConfigManager

@Module
class CollectModule(val application: Application,
                    val db: ConfigRoomDatabase,
                    val userSession: UserSession,
                    val config: Config) {

    @Provides
    fun providesUserSession(): UserSession = userSession

    @Provides
    fun providesCollectManager(configManager: ConfigManager, configRepository: ConfigRepository): CollectManager = LiveCollectManager(application, configManager, configRepository, userSession)

    @Provides
    fun providesConfigManager(configRepository: ConfigRepository): ConfigManager = LiveConfigManager(configRepository, config.id)

    @Provides
    fun providesConfigRepository() = ConfigRepository(application, db)

    @Provides
    fun providesConfig(): Config = config
}