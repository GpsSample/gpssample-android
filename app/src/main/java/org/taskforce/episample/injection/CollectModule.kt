package org.taskforce.episample.injection

import android.app.Application
import dagger.Module
import dagger.Provides
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.core.language.LanguageService
import org.taskforce.episample.core.language.LiveLanguageService
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.ConfigRoomDatabase
import org.taskforce.episample.db.config.LiveCollectManager
import org.taskforce.episample.managers.LiveConfigManager

@Module
class CollectModule(val application: Application,
                    val db: ConfigRoomDatabase,
                    val userSession: UserSession,
                    val config: Config) {

    private val mockNavigationManagerInstance = MockNavigationManager()

    @Provides
    fun providesUserSession(): UserSession = userSession

    @Provides
    fun providesCollectManager(configManager: ConfigManager, configRepository: ConfigRepository): CollectManager = LiveCollectManager(application, configManager, configRepository, userSession)

    @Provides
    fun providesNavigationManager(): NavigationManager = mockNavigationManagerInstance

    @Provides
    fun providesConfigManager(configRepository: ConfigRepository): ConfigManager = LiveConfigManager(configRepository, config.id)

    @Provides
    fun providesConfigRepository() = ConfigRepository(application, db)

    @Provides
    fun providesConfig(): Config = config

    // TODO use config custom languages when providing language service
    @Provides
    fun providesLanguageService(): LanguageService = LiveLanguageService(application, config.enumerationSubject, listOf())

    @Provides
    fun providesLocationService(): LocationService = LiveLocationService(application)
}