package org.taskforce.episample.collection.viewmodels

import org.taskforce.episample.core.interfaces.CollectItem
import org.taskforce.episample.core.interfaces.DisplaySettings
import org.taskforce.episample.utils.DateUtil


class CollectItemViewModel(val icon: String?,
                           val title: String?,
                           val description: String,
                           val incompleteText: String,
                           val isIncomplete: Boolean) {

    constructor(collectItem: CollectItem, icon: String?, incompleteText: String, displaySettings: DisplaySettings) : this(
            icon,
            collectItem.title,
            getDateString(collectItem, displaySettings),
            incompleteText,
            collectItem.isIncomplete
    )
    
    companion object {
        fun getDateString(collectItem: CollectItem, displaySettings: DisplaySettings): String {
            return "${DateUtil.getFormattedDate(collectItem.dateCreated, displaySettings)}, " +
                    "${DateUtil.getFormattedTime(collectItem.dateCreated, displaySettings)}"
        }
    }
    
}