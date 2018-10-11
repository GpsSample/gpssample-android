package org.taskforce.episample.sampling

import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.taskforce.episample.config.sampling.SamplingMethod
import org.taskforce.episample.db.collect.ResolvedEnumeration

@RunWith(AndroidJUnit4::class)
class SamplingTest {

    val enumerations = mutableListOf<ResolvedEnumeration>()

    @Before
    fun createEnumerations() {
        (1..100).forEach { id ->
            val enumeration = ResolvedEnumeration(id.toString(), 1.0, 1.0, "Note", false, false, 5.0, "John Doe", "Some Title", "Image")
            enumerations.add(enumeration)
        }
    }

    /*
        Before you alter this, please note the following:
        All the following tests are based on a setup where a configuration with an integer custom field has been added, along with 10 enumerations
        where the custom field value which has been added is an integer taking the values [1..10] have been added

        So for example, the test filterLessThan is attempting to filter the results less than the value 5
        So we would expect enumerations with the following values to be found [1, 2, 3, 4] for a size of 4

        For filterGreaterThanEqualTo, it is attempting to filter results that are greater than or equal to 5
        So we would expect enumerations with the following values to be found [5, 6, 7, 8, 9, 10] for a size of 6

        And so on and so forth.
     */

    @Test
    @Throws(Exception::class)
    fun sampleLessThanNumberOfHouseholds() {
        val sampleSize = 21
        val simpleSample = SamplingMethod.simpleRandomSample(sampleSize, enumerations)
        val systematicSample = SamplingMethod.systematicRandomSample(sampleSize, enumerations)

        Assert.assertEquals(sampleSize, simpleSample.size)
        Assert.assertEquals(sampleSize, systematicSample.size)
    }

    @Test
    @Throws(Exception::class)
    fun sampleALittleLessThanNumberOfHouseholds() {
        val sampleSize = 99
        val simpleSample = SamplingMethod.simpleRandomSample(sampleSize, enumerations)
        val systematicSample = SamplingMethod.systematicRandomSample(sampleSize, enumerations)

        Assert.assertEquals(sampleSize, simpleSample.size)
        Assert.assertEquals(sampleSize, systematicSample.size)
    }

    @Test
    @Throws(Exception::class)
    fun sampleAllHouseholds() {
        val sampleSize = 100
        val simpleSample = SamplingMethod.simpleRandomSample(sampleSize, enumerations)
        val systematicSample = SamplingMethod.systematicRandomSample(sampleSize, enumerations)

        Assert.assertEquals(sampleSize, simpleSample.size)
        Assert.assertEquals(sampleSize, systematicSample.size)
    }

    @Test
    @Throws(Exception::class)
    fun sampleMoreThanAllHouseholds() {
        val sampleSize = 120
        val simpleSample = SamplingMethod.simpleRandomSample(sampleSize, enumerations)
        val systematicSample = SamplingMethod.systematicRandomSample(sampleSize, enumerations)

        Assert.assertEquals(enumerations.size, simpleSample.size)
        Assert.assertEquals(enumerations.size, systematicSample.size)
    }
}