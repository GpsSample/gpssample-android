package org.taskforce.episample.config.base

import org.taskforce.episample.config.fields.CustomField
import org.taskforce.episample.config.geography.Geography
import org.taskforce.episample.config.geography.OfflineTile
import org.taskforce.episample.config.language.CustomLanguage
import org.taskforce.episample.config.sampling.SamplingMethod
import org.taskforce.episample.config.settings.admin.AdminSettings
import org.taskforce.episample.config.settings.display.DisplaySettings
import org.taskforce.episample.config.settings.server.ServerSettings
import org.taskforce.episample.config.settings.user.UserSettings
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

    var completeness = 1
    var userSettings: UserSettings? = null
    var serverSettings: ServerSettings? = null
    var samplingMethod: SamplingMethod? = null
    var offlineTiles: Collection<OfflineTile>? = null
    var landmarkTypes = listOf<LandmarkType>()
    var geography: Geography? = null
    var displaySettings = DisplaySettings()
    var customLanguages = listOf<CustomLanguage>()
    var customFields = listOf<CustomField>()
    var adminSettings: AdminSettings? = null
    var enumerationSubject: String? = null

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