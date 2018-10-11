package org.taskforce.episample.db.transfer

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Transaction
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
            enumerations: List<Enumeration>,
            landmarks: List<Landmark>,
            breadcrumbs: List<GpsBreadcrumb>,
            customFieldValues: List<CustomFieldValue>,
            navigationPlans: List<NavigationPlan>,
            navigationItems: List<NavigationItem>,
            samples: List<SampleEntity>,
            sampleEnumerations: List<SampleEnumerationEntity>,
            sampleWarnings: List<WarningEntity>
            ) {
        insertEnumerations(enumerations)
        insertLandmarks(landmarks)
        insertBreadcrumbs(breadcrumbs)
        insertCustomFieldValues(customFieldValues)
        insertNavigationPlans(navigationPlans)
        insertNavigationItems(navigationItems)
        insertSamples(samples)
        insertSampleEnumerations(sampleEnumerations)
        insertSampleWarnings(sampleWarnings)
    }
}