package org.taskforce.episample.db.dao

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.db.StudyRoomDatabase
import org.taskforce.episample.db.collect.GpsBreakcrumbDao
import org.taskforce.episample.db.collect.ResolvedEnumerationDao
import org.taskforce.episample.db.config.ConfigDao
import org.taskforce.episample.db.config.ResolvedConfigDao
import org.taskforce.episample.db.config.ResolvedStudyDao
import org.taskforce.episample.db.config.StudyDao
import org.taskforce.episample.db.config.customfield.CustomFieldDao
import org.taskforce.episample.db.config.customfield.CustomFieldValueDao
import org.taskforce.episample.db.navigation.NavigationDao
import org.taskforce.episample.db.navigation.NavigationItem
import org.taskforce.episample.db.navigation.NavigationPlan
import org.taskforce.episample.db.utils.CommonSetup
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class NavigationDaoTest {

    private var navigationDao: NavigationDao? = null
    private var configDao: ConfigDao? = null
    private var resolvedConfigDao: ResolvedConfigDao? = null
    private var studyDao: StudyDao? = null
    private var resolvedStudyDao: ResolvedStudyDao? = null
    private var breadcrumbDao: GpsBreakcrumbDao? = null
    private var customFieldDao: CustomFieldDao? = null
    private var customFieldValueDao: CustomFieldValueDao? = null
    private var resolvedEnumerationDao: ResolvedEnumerationDao? = null
    private var db: StudyRoomDatabase? = null
    lateinit var studyId: String

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(context, StudyRoomDatabase::class.java).build()
        configDao = db!!.configDao()
        studyDao = db!!.studyDao()
        resolvedConfigDao = db!!.resolvedConfigDao()
        breadcrumbDao = db!!.breadcrumbDao()
        resolvedStudyDao = db!!.resolvedStudyDao()
        customFieldDao = db!!.customFieldDao()
        customFieldValueDao = db!!.customFieldValueDao()
        resolvedEnumerationDao = db!!.resolvedEnumerationDao()
        navigationDao = db!!.navigationDao()

        val insertConfigId = UUID.randomUUID().toString()
        CommonSetup.setupConfig(configDao!!, insertConfigId)

        val resolvedConfig = resolvedConfigDao!!.getConfigSync(insertConfigId)
        studyId = UUID.randomUUID().toString()

        studyDao!!.insert(studyId, "Study 1", "Study Password", resolvedConfig)

        for (i in 1..10) {
            val enumerationId = UUID.randomUUID().toString()
            val insertEnumeration = CommonSetup.makeEnumeration(studyId, enumerationId)
            studyDao?.insert(insertEnumeration)
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db!!.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeNavigationPlan() {
        val navigationPlanTitle = "Navigation Plan Title"
        val insertNavigationPlan = NavigationPlan(studyId, navigationPlanTitle)
        navigationDao?.insertNavigationPlan(insertNavigationPlan)
        assertEquals(navigationPlanTitle, navigationDao?.getNavigationPlanSync(insertNavigationPlan.id)?.title)

        val enumerations = resolvedEnumerationDao!!.getResolvedEnumerationsSync(studyId)
        enumerations.forEachIndexed { index, enumeration ->
            navigationDao?.insertNavigationItem(NavigationItem(insertNavigationPlan.id, enumeration.id, index + 1, SurveyStatus.Incomplete()))
        }

        assertEquals(enumerations.size, navigationDao?.getNavigationPlanSync(insertNavigationPlan.id)?.navigationItems?.size)
    }


    @Test
    @Throws(Exception::class)
    fun fillOutNavigationPlanAsProblems() {
        val navigationPlanTitle = "Navigation Plan Title"
        val insertNavigationPlan = NavigationPlan(studyId, navigationPlanTitle)
        navigationDao?.insertNavigationPlan(insertNavigationPlan)
        assertEquals(navigationPlanTitle, navigationDao?.getNavigationPlanSync(insertNavigationPlan.id)?.title)

        val enumerations = resolvedEnumerationDao!!.getResolvedEnumerationsSync(studyId)
        enumerations.forEachIndexed { index, enumeration ->
            navigationDao?.insertNavigationItem(NavigationItem(insertNavigationPlan.id, enumeration.id, index + 1, SurveyStatus.Incomplete()))
        }

        // ^Setup Complete
        val navigationItems = navigationDao!!.getNavigationPlanSync(insertNavigationPlan.id).navigationItems
        navigationItems.forEachIndexed { index, item ->
            navigationDao?.updateNavigationItem(item.id, SurveyStatus.Problem("Problem reason $index"))
        }

        val updatedNavigationItems = navigationDao!!.getNavigationPlanSync(insertNavigationPlan.id).navigationItems
        updatedNavigationItems.forEach {
            assertTrue(it.surveyStatus is SurveyStatus.Problem)
        }
    }

    @Test
    @Throws(Exception::class)
    fun fillOutNavigationPlanAsComplete() {
        val navigationPlanTitle = "Navigation Plan Title"
        val insertNavigationPlan = NavigationPlan(studyId, navigationPlanTitle)
        navigationDao?.insertNavigationPlan(insertNavigationPlan)
        assertEquals(navigationPlanTitle, navigationDao?.getNavigationPlanSync(insertNavigationPlan.id)?.title)

        val enumerations = resolvedEnumerationDao!!.getResolvedEnumerationsSync(studyId)
        enumerations.forEachIndexed { index, enumeration ->
            navigationDao?.insertNavigationItem(NavigationItem(insertNavigationPlan.id, enumeration.id, index + 1, SurveyStatus.Incomplete()))
        }

        // ^Setup Complete
        val navigationItems = navigationDao!!.getNavigationPlanSync(insertNavigationPlan.id).navigationItems
        navigationItems.forEach { item ->
            navigationDao?.updateNavigationItem(item.id, SurveyStatus.Complete())
        }

        val updatedNavigationItems = navigationDao!!.getNavigationPlanSync(insertNavigationPlan.id).navigationItems
        updatedNavigationItems.forEach {
            assertTrue(it.surveyStatus is SurveyStatus.Complete)
        }
    }
}