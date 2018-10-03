package org.taskforce.episample.config.sampling

import java.io.Serializable

data class SamplingMethod(val type: SamplingMethodology,
                          val units: SamplingUnits,
                          val grouping: SamplingGrouping) : Serializable {
    companion object {
        val DEFAULT_METHOD = SamplingMethod(SamplingMethodology.SIMPLE_RANDOM_SAMPLE, SamplingUnits.PERCENT, SamplingGrouping.SUBSETS)
    }
}

enum class SamplingMethodology(val displayText: String) : Serializable {
    SIMPLE_RANDOM_SAMPLE("Simple Random Sampling"),
    SYSTEMATIC_RANDOM_SAMPLE("Systematic Random Sampling")
}

sealed class SamplingUnits(val name: String) : Serializable {
    class SamplingPercentage(var sampleSize: Double = 0.0) : SamplingUnits("PERCENTAGE"), Serializable
    class SamplingFixedAmount(var sampleSize: Int = 0) : SamplingUnits("FIXED"), Serializable

    companion object {
        fun values(): List<SamplingUnits> = listOf(PERCENT, FIXED)

        val PERCENT = SamplingPercentage()
        val FIXED = SamplingFixedAmount()
    }
}

enum class SamplingGrouping {
    SUBSETS,
    STRATA,
    NONE
}