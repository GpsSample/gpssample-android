package org.taskforce.episample.collection.viewmodels

import org.taskforce.episample.core.interfaces.CollectItem

class CollectDetailField(val data: CollectItem,
                         val name: String,
                         val value: String,
                         val isCheckbox: Boolean = false,
                         var isIncomplete: Boolean = false,
                         var showLandmarkType: Boolean = false)