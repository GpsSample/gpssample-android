package org.taskforce.episample.config.sampling

import java.util.*

class SamplingManager(private val randomNumberSeed: Long = generateRandomSeed(),
                      private val currentTarget: Int = 0) {

    private val generator = Random(randomNumberSeed)

    init {
        accelerateToCurrent()
    }

    private fun accelerateToCurrent() {
        for (i in 0..currentTarget) {
            generator.nextInt()
        }
    }

    companion object {
        private fun generateRandomSeed() = System.currentTimeMillis()
    }
}