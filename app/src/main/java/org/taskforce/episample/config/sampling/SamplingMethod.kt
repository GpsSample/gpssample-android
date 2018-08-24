package org.taskforce.episample.config.sampling

import java.io.Serializable

data class SamplingMethod(val type: SamplingSelectionType,
                          val inputType: SamplingSelectionInputType,
                          val sampleSize: Double) : Serializable

enum class SamplingSelectionType(val displayText: String) {
    SIMPLE_RANDOM_SAMPLE("Simple Random Sampling"),
    SYSTEMATIC_RANDOM_SAMPLE("Systematic Random Sampling")
}

enum class SamplingSelectionInputType {
    PERCENT,
    FIXED
}