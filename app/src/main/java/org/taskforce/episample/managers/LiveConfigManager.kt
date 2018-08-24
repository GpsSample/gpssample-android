package org.taskforce.episample.managers

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.db.ConfigRepository
import org.taskforce.episample.db.config.ResolvedConfig

class LiveConfigManager(application: Application, override val configId: String): ConfigManager {

    private val configRepository = ConfigRepository(application)

    override fun getConfig(): LiveData<Config> {
        return Transformations.map(configRepository.getResolvedConfig(configId), {
            return@map org.taskforce.episample.managers.ResolvedConfig(it)
        })
    }
}

class ResolvedConfig(dbConfig: ResolvedConfig): Config {
    override var name: String = dbConfig.name
    override val dateCreated = dbConfig.dateCreated
    override val id = dbConfig.id
    override var adminSettings = dbConfig.adminSettings as AdminSettings
    override var enumerationSubject: EnumerationSubject = dbConfig.enumerationSubject
    override var customFields: List<CustomField> = dbConfig.customFields.map { it as CustomField }
}