package org.taskforce.episample.db

import android.os.Bundle
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.taskforce.episample.core.navigation.SurveyStatus

@RunWith(AndroidJUnit4::class)
class SurveyStatusParcelTest {
    @Test
    fun testParcel() {
        val problem = SurveyStatus.Problem("problem")
        val incomplete = SurveyStatus.Incomplete()
        val skip = SurveyStatus.Skipped("skip")
        val complete = SurveyStatus.Complete()

        val bundle = Bundle()
        bundle.putParcelable("problem", problem)
        bundle.putParcelable("incomplete", incomplete)
        bundle.putParcelable("skip", skip)
        bundle.putParcelable("complete", complete)

        bundle.putParcelable("problemsealed", problem as SurveyStatus)
        bundle.putParcelable("incompletesealed", incomplete as SurveyStatus)
        bundle.putParcelable("skipsealed", skip as SurveyStatus)
        bundle.putParcelable("completesealed", complete as SurveyStatus)

        Assert.assertEquals(problem, bundle.getParcelable("problem"))
        Assert.assertEquals(incomplete, bundle.getParcelable("incomplete"))
        Assert.assertEquals(skip, bundle.getParcelable("skip"))
        Assert.assertEquals(complete, bundle.getParcelable("complete"))
        Assert.assertEquals(problem, bundle.getParcelable("problemsealed"))
        Assert.assertEquals(incomplete, bundle.getParcelable("incompletesealed"))
        Assert.assertEquals(skip, bundle.getParcelable("skipsealed"))
        Assert.assertEquals(complete, bundle.getParcelable("completesealed"))
    }
}