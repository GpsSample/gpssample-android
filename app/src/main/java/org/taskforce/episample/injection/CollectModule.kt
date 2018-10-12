package org.taskforce.episample.injection

import android.app.Application
import dagger.Module
import dagger.Provides
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.core.language.LanguageService
import org.taskforce.episample.core.language.LiveLanguageService
import org.taskforce.episample.db.StudyRepository
import org.taskforce.episample.db.StudyRoomDatabase
import org.taskforce.episample.db.config.LiveCollectManager
import org.taskforce.episample.managers.LiveConfigManager

@Module
class CollectModule(val application: Application,
                    val studyDb: StudyRoomDatabase,
                    val userSession: UserSession,
                    val config: Config) {

    @Provides
    fun providesUserSession(): UserSession = userSession

    @Provides
    fun providesCollectManager(configManager: ConfigManager, studyRepository: StudyRepository): CollectManager = LiveCollectManager(application, configManager, config, studyRepository, userSession)

    @Provides
    fun providesNavigationManager(studyRepository: StudyRepository): NavigationManager = LiveNavigationManager(application, config, studyRepository, userSession)

    @Provides
    fun providesConfigManager(studyRepository: StudyRepository): ConfigManager = LiveConfigManager(studyRepository, config.id)

    @Provides
    fun providesStudyRepository() = StudyRepository(application, studyDb)

    @Provides
    fun providesConfig(): Config = config

    // TODO use config custom languages when providing language service
    @Provides
    fun providesLanguageService(): LanguageService = LiveLanguageService(application, config.enumerationSubject, listOf())

    @Provides
    fun providesLocationService(): LocationService = LiveLocationService(application, userSession.username, config.userSettings)
}