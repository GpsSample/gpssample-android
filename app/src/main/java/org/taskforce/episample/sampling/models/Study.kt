package org.taskforce.episample.sampling.models

import org.taskforce.episample.collection.models.CollectItem
import org.taskforce.episample.collection.models.EnumerationItem
import org.taskforce.episample.collection.models.GpsBreadcrumb
import org.taskforce.episample.collection.models.LandmarkItem
import org.taskforce.episample.config.base.Config
import java.io.Serializable
import java.util.*

class Study(
        val name: String,
        val password: String,
        val config: Config,
        val dateCreated: Date = Date(),
        val id: String = UUID.randomUUID().toString()): Serializable {

    val dateCreatedDisplay
        get() = config.displaySettings.getFormattedDate(dateCreated, true)

    var gpsBreadcrumbs = mutableListOf<GpsBreadcrumb>()

    var landmarks = mutableListOf<LandmarkItem>()

    var enumerationItems = mutableListOf<EnumerationItem>()

    val collectItems: List<CollectItem>
        get() = landmarks + enumerationItems

}