package org.taskforce.episample.injection

import dagger.Module
import dagger.Provides
import org.taskforce.episample.study.managers.LiveStudyStorage
import org.taskforce.episample.study.managers.StudyStorage

@Module
class StudyStorageModule {

    @Provides
    fun provideStudyStorage() : StudyStorage = LiveStudyStorage()
}
