package org.taskforce.episample.db.transfer

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import org.taskforce.episample.db.navigation.NavigationItem

@Dao
interface TransferNavigationItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNavigationItems(navigationItems: List<NavigationItem>)

    @Query("SELECT * from navigation_item_table")
    fun getNavigationItems(): List<NavigationItem>
}