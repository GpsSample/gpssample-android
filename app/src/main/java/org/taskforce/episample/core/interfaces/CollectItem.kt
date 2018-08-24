package org.taskforce.episample.core.interfaces

interface CollectItem: Breadcrumb {
    val isIncomplete: Boolean
    val title: String?
    val note: String?
    val image: String?

    val displayDate: String
        get() = dateCreated.toString()
}