package org.taskforce.episample.db.config

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface EnumerationAreaDao {
    
    @Insert
    fun insertEnumerationAreas(vararg enumerationArea: EnumerationArea)
    
    @Insert
    fun insertEnumerationAreaPoints(vararg enumerationAreaPoint: EnumerationAreaPoint)

    @Query("SELECT * FROM enumeration_area_table WHERE config_id LIKE :configId")
    fun getEnumerationsAreas(configId: String): LiveData<List<ResolvedEnumerationArea>>

    @Query("SELECT * FROM enumeration_area_table WHERE config_id LIKE :configId")
    fun getEnumerationAreasSync(configId: String): List<ResolvedEnumerationArea>
}