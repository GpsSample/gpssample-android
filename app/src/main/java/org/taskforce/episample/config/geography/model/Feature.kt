package org.taskforce.episample.config.geography.model

data class Feature(val type: String,
                   val geometry: Geometry,
                   val properties: Map<String, String>)