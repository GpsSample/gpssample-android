package org.taskforce.episample

import android.Manifest
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import io.reactivex.Single
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.taskforce.episample.config.base.Config
import org.taskforce.episample.config.base.ConfigStorage
import org.taskforce.episample.mock.MockEpiApplication
import org.taskforce.episample.splash.SplashActivity
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class TransferDialogTest {

    @Inject
    lateinit var configStorage: ConfigStorage

    @Rule
    @JvmField
    var mActivityRule = ActivityTestRule(SplashActivity::class.java, true, false)

    @Before
    fun setUp() {
        val app = InstrumentationRegistry.getTargetContext().applicationContext as MockEpiApplication
        val testComponent = app.component as MockEpiApplication.TestComponent
        testComponent.inject(this)

        val configResult = Single.fromCallable { listOf<Config>() }
        Mockito.`when`(configStorage.loadConfigsFromDisk())
                .thenReturn(configResult)
    }


    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @Test
    fun initialTransferDialogViewModelState() {
        mActivityRule.launchActivity(Intent())

        onView(withText(R.string.config_new))
                .perform(click())

        onView(withId(R.id.nameInput)).perform(typeText("Anything"))

        onView(withId(R.id.next))
                .perform(click())

        val textView = onView(
                allOf(withId(R.id.downloadButton), withText("GET FILE FROM URL"),
                        childAtPosition(
                                allOf(withId(R.id.transfer),
                                        childAtPosition(
                                                withClassName(`is`<String>("android.widget.RelativeLayout")),
                                                1)),
                                1)))


        textView.perform(scrollTo(), click())

        onView(withId(R.id.urlInput)).check(matches(withText("")))

        onView(withText(R.string.config_transfer_dialog_download)).check(matches(not(isEnabled())))
    }

    @Test
    fun enablingDownloadButton() {
        mActivityRule.launchActivity(Intent())

        onView(withText(R.string.config_new))
                .perform(click())

        onView(withId(R.id.nameInput)).perform(typeText("Anything"))

        onView(withId(R.id.next))
                .perform(click())

        val textView = onView(
                allOf(withId(R.id.downloadButton), withText("GET FILE FROM URL"),
                        childAtPosition(
                                allOf(withId(R.id.transfer),
                                        childAtPosition(
                                                withClassName(`is`<String>("android.widget.RelativeLayout")),
                                                1)),
                                1)))


        textView.perform(scrollTo(), click())

        onView(withId(R.id.urlInput))
                .perform(typeText("foo"))

        onView(withText(R.string.config_transfer_dialog_download)).check(matches(isEnabled()))
    }

}

// TODO find out why espresgsso cannot find this unique view by ID
private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int): Matcher<View> {

    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("Child at position $position in parent ")
            parentMatcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            val parent = view.parent
            return (parent is ViewGroup && parentMatcher.matches(parent)
                    && view == parent.getChildAt(position))
        }
    }
}