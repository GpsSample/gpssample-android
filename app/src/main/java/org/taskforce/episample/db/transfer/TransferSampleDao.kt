package org.taskforce.episample.db.transfer

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import org.taskforce.episample.db.sampling.SampleEntity
import org.taskforce.episample.db.sampling.SampleEnumerationEntity
import org.taskforce.episample.db.sampling.WarningEntity

@Dao
interface TransferSampleDao {
    @Query("SELECT * FROM samples")
    fun getSamples(): List<SampleEntity>

    @Query("SELECT * FROM sample_enumerations")
    fun getSampleEnumerations(): List<SampleEnumerationEntity>

    @Query("SELECT * FROM sample_warnings")
    fun getSampleWarnings(): List<WarningEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSamples(samples: List<SampleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSampleEnumerations(sampleEnumerations: List<SampleEnumerationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSampleWarnings(warnings: List<WarningEntity>)
}
