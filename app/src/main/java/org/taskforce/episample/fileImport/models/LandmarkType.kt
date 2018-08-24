package org.taskforce.episample.fileImport.models

import java.io.Serializable
import java.util.*

data class LandmarkType(var name: String,
                        var iconLocation: String? = null,
                        var id: String = UUID.randomUUID().toString()) : Serializable