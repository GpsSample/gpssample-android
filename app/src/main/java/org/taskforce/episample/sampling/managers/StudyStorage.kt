package org.taskforce.episample.sampling.managers

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.taskforce.episample.sampling.models.Study
import java.io.*

interface StudyStorage {
    fun loadStudyFromDisk(): Study?
    fun writeStudyToDisk(study: Study?): Single<Boolean>
}

class LiveStudyStorage: StudyStorage {

    override fun loadStudyFromDisk() =
            File(StudyManager.studyDirectory).listFiles()?.firstOrNull {
                it.name.removePrefix(it.nameWithoutExtension) == StudyManager.studyExtension
            }?.run {
                val stream = ObjectInputStream(FileInputStream(this))
                val study = stream.readObject() as Study
                stream.close()
                study
            }

    override fun writeStudyToDisk(currentStudy: Study?): Single<Boolean> =
            Single.fromCallable {
                File(StudyManager.studyDirectory).mkdirs()
                ObjectOutputStream(
                        FileOutputStream(StudyManager.studyDirectory + currentStudy?.name?.replace("\\s".toRegex(), "") + StudyManager.studyExtension)
                ).apply {
                    writeObject(currentStudy)
                    close()
                }
                true
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

}