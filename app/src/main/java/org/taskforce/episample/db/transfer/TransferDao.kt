package org.taskforce.episample.db.transfer

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Transaction
import org.taskforce.episample.core.navigation.SurveyStatus
import org.taskforce.episample.db.collect.Enumeration
import org.taskforce.episample.db.collect.GpsBreadcrumb
import org.taskforce.episample.db.collect.Landmark
import org.taskforce.episample.db.config.customfield.CustomFieldValue
import org.taskforce.episample.db.navigation.NavigationItem
import org.taskforce.episample.db.navigation.NavigationPlan
import org.taskforce.episample.db.sampling.SampleEntity
import org.taskforce.episample.db.sampling.SampleEnumerationEntity
import org.taskforce.episample.db.sampling.WarningEntity

@Dao
abstract class TransferDao: TransferEnumerationDao, TransferLandmarkDao, TransferBreadcrumbDao,
        TransferCustomFieldValueDao, TransferNavigationPlanDao, TransferNavigationItemDao, TransferSampleDao {

    @Transaction
    open fun transfer(
            overrideEnumerations: List<Enumeration>,
            insertIgnoreConflictsEnumerations: List<Enumeration>,
            landmarks: List<Landmark>,
            breadcrumbs: List<GpsBreadcrumb>,
            customFieldValues: List<CustomFieldValue>,
            samples: List<SampleEntity>,
            sampleEnumerations: List<SampleEnumerationEntity>,
            sampleWarnings: List<WarningEntity>,
            targetNavigationPlans: List<NavigationPlan>,
            sourceNavigationPlans: List<NavigationPlan>,
            targetNavigationItems: List<NavigationItem>,
            sourceNavigationItems: List<NavigationItem>
            ) {
        insertEnumerations(overrideEnumerations)
        insertIgnoringConflicts(insertIgnoreConflictsEnumerations)
        insertLandmarks(landmarks)
        insertBreadcrumbs(breadcrumbs)
        insertCustomFieldValues(customFieldValues)
        insertSamples(samples)
        insertSampleEnumerations(sampleEnumerations)
        insertSampleWarnings(sampleWarnings)
        insertNavigationPlans(targetNavigationPlans)
        insertNavigationPlans(sourceNavigationPlans)
        insertNavigationItems(targetNavigationItems)
        insertNavigationItems(sourceNavigationItems.filter { it.surveyStatus !is SurveyStatus.Incomplete })
    }
}