package org.taskforce.episample.core.interfaces

interface Enumeration: CollectItem {
    val customFieldValues: List<CustomFieldValue>
    val excluded: Boolean
}