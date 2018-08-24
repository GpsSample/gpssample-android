package org.taskforce.episample.config.base

import android.os.Environment
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.taskforce.episample.BuildConfig
import java.io.*

interface ConfigStorage {
    fun deleteConfigFromDisk(config: Config): Completable
    fun loadConfigsFromDisk(): Single<List<Config>>
    fun writeConfigToDisk(config: Config): Completable
}

class LiveConfigStorage : ConfigStorage {

    override fun deleteConfigFromDisk(config: Config): Completable =
            Completable.fromCallable {
                File(configStorageDirectory + config.name.replace("\\s".toRegex(), "") + configExtension).delete()
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())


    override fun loadConfigsFromDisk(): Single<List<Config>> =
            Single.fromCallable({
                File(configStorageDirectory).listFiles()?.filter {
                    it.name.removePrefix(it.nameWithoutExtension) == configExtension
                }?.map {
                    val stream = ObjectInputStream(FileInputStream(it))
                    val config = stream.readObject() as Config
                    stream.close()
                    config
                } ?: listOf()
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun writeConfigToDisk(config: Config): Completable =
            Completable.fromCallable {
                File(configStorageDirectory).mkdirs()
                ObjectOutputStream(
                        FileOutputStream(configStorageDirectory + config.name.replace("\\s".toRegex(), "") + configExtension)
                ).apply {
                    writeObject(config)
                    close()
                }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    companion object {
        private val configDirectory = "${Environment.getExternalStorageDirectory()}/${BuildConfig.FILE_DIRECTORY_NAME}/Config/"
        internal val configStorageDirectory = "${configDirectory.removeSuffix("/")}/configs/"
        internal val configExtension = ".config"
    }
}