package org.taskforce.episample.injection

import android.content.Context
import dagger.Module
import dagger.Provides
import org.taskforce.episample.collection.managers.GpsBreadcrumbManager
import org.taskforce.episample.config.base.ConfigManager
import org.taskforce.episample.config.base.ConfigStorage
import org.taskforce.episample.config.base.LiveConfigManager
import org.taskforce.episample.config.landmark.LandmarkTypeManager
import org.taskforce.episample.config.transfer.TransferFileService
import org.taskforce.episample.config.transfer.TransferManager
import org.taskforce.episample.core.interfaces.LiveLocationService
import org.taskforce.episample.permissions.managers.PermissionManager
import org.taskforce.episample.sampling.managers.StudyManager
import org.taskforce.episample.sampling.managers.StudyStorage
import org.taskforce.episample.supervisor.upload.managers.DriveUploadManager
import org.taskforce.episample.supervisor.upload.managers.UploadManager
import org.taskforce.episample.sync.managers.SyncManager
import org.taskforce.episample.toolbar.managers.LanguageManager
import org.taskforce.episample.utils.getLanguageManager
import javax.inject.Singleton

@Module
class EpiModule(private val context: Context) {

    private val languageManager by lazy {
        context.getLanguageManager()
    }

    @Singleton
    @Provides
    fun provideTransferManager() = TransferManager(context)

    @Singleton
    @Provides
    fun provideLanguageManager() = languageManager

    @Singleton
    @Provides
    fun provideStudyManager(configManager: ConfigManager, studyStorage: StudyStorage) = StudyManager(configManager, studyStorage)

    @Provides
    fun providePermissionManager() = PermissionManager(context, languageManager)

    @Singleton
    @Provides
    fun provideConfigManager(configStorage: ConfigStorage): ConfigManager  = LiveConfigManager(
            configStorage)

    @Provides
    fun provideTransferService() = TransferFileService()

    @Provides
    fun provideGpsBreadcrumbManager(studyManager: StudyManager) = GpsBreadcrumbManager(studyManager)

    @Provides
    @Singleton
    fun provideLandmarkTypeManager(languageManager: LanguageManager) = LandmarkTypeManager(context, languageManager)

    @Provides
    @Singleton
    fun provideSyncManager() = SyncManager()
    
    @Provides
    @Singleton
    fun provideUploadManager(): UploadManager {
        return DriveUploadManager()
    }

    @Provides
    fun providesLocationService(): LiveLocationService = LiveLocationService(context, "", org.taskforce.episample.db.config.UserSettings(30.0, 50.0, true, 50, ""))
}