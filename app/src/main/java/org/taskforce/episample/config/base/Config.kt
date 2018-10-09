package org.taskforce.episample.config.base

import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.config.geography.OfflineTile
import org.taskforce.episample.config.language.CustomLanguage
import org.taskforce.episample.config.sampling.SamplingMethod
import org.taskforce.episample.config.settings.admin.AdminSettings
import org.taskforce.episample.config.settings.display.DisplaySettings
import org.taskforce.episample.config.settings.server.ServerSettings
import org.taskforce.episample.config.settings.user.UserSettings
import org.taskforce.episample.core.interfaces.EnumerationArea
import org.taskforce.episample.core.interfaces.EnumerationSubject
import org.taskforce.episample.db.filter.RuleRecord
import org.taskforce.episample.db.filter.RuleSet
import org.taskforce.episample.fileImport.models.LandmarkType
import org.taskforce.episample.utils.humanReadableBytes
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

class Config(
        var dateCreated: Date = Date(),
        var name: String = "",
        var id: String = UUID.randomUUID().toString()
) : Serializable {

    data class CustomLandmarkTypeInput(val name: String, val iconLocation: String)

    var completeness = 1
    var userSettings: UserSettings? = null
    var serverSettings: ServerSettings? = null
    var samplingMethod: SamplingMethod = SamplingMethod.DEFAULT_METHOD
    var offlineTiles: Collection<OfflineTile>? = null
    var landmarkTypes = listOf<LandmarkType>()
    var customLandmarkTypes = listOf<CustomLandmarkTypeInput>()
    var enumerationAreas: List<EnumerationArea> = listOf()
    var displaySettings = DisplaySettings()
    var customLanguages = listOf<CustomLanguage>()
    var customFields = listOf<CustomField>()
    var adminSettings: AdminSettings? = null
    var enumerationSubject: EnumerationSubject? = null
    var ruleSets = listOf<RuleSet>()
    var rules = listOf<RuleRecord>()

    val size: String
        get() {
            val sizeStream = ByteArrayOutputStream()
            ObjectOutputStream(sizeStream).apply {
                writeObject(this@Config)
                flush()
                close()
            }
            return humanReadableBytes(sizeStream.size().toLong())
        }

    val displayDateCreated
        get() = displaySettings.getFormattedDate(dateCreated, true)


    companion object {
        const val nameMaxChars = 32
        const val nameMinChars = 4
    }
}