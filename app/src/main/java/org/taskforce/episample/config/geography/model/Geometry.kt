package org.taskforce.episample.config.geography.model

data class Geometry(val type: String,
                    val coordinates: List<List<List<Double>>>) {
}