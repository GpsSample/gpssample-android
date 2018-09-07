package org.taskforce.episample.collection.viewmodels

import android.annotation.SuppressLint
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.Observable
import android.databinding.ObservableField
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import org.taskforce.episample.collection.ui.CollectGpsPrecisionViewModel
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.utils.DateUtil
import org.taskforce.episample.utils.bindDelegate

class CollectCardViewModel(userSettings: UserSettings?,
                           enumerationSubject: EnumerationSubject?,
                           displaySettings: DisplaySettings?,
                           lastKnownLocationObservable: io.reactivex.Observable<Pair<LatLng, Float>>,
                           lowestColor: Int,
                           mediumColor: Int,
                           highestColor: Int) : BaseObservable() {
    
    init {
        lastKnownLocationObservable.subscribe { 
            currentLocation.set(it.first)
        }
    }
    
    var itemData = object: ObservableField<CollectItem>() {
        override fun set(value: CollectItem?) {
            super.set(value)
            
            value?.gpsPrecision?.let { gpsPrecisionVm.precision.set(it) }
        }
    }
    
    var currentLocation = ObservableField<LatLng>()
    
    @get:Bindable
    val imageUrl = object: ObservableField<String>(itemData) {
        override fun get(): String? {
            return itemData.get()?.image
        }
    }
    
    @get:Bindable
    var itemType = object: ObservableField<String>(itemData) {
        override fun get(): String? = when (itemData.get()) {
            is Enumeration -> enumerationSubject?.singular ?: ""
            is Landmark -> "Landmark"
            else -> ""
        }.toUpperCase()
    }
    
    @get:Bindable
    val title = object: ObservableField<String>(itemData) {
        override fun get(): String? = itemData.get()?.title
    }
    
    @get:Bindable
    val addedDate = object: ObservableField<String>(itemData) {
        @SuppressLint("SimpleDateFormat")
        override fun get(): String? {
            return itemData.get()?.dateCreated?.let { 
                "Added ${DateUtil.getFormattedDate(it, displaySettings)} at " +
                        "${DateUtil.getFormattedTime(it, displaySettings)}"
            }
        }
    }
    
    @get:Bindable
    val showIncomplete = object: ObservableField<Boolean>(itemData) {
        override fun get(): Boolean? = (itemData.get() as? Enumeration)?.isIncomplete
    }

    @get:Bindable
    val showExcluded = object: ObservableField<Boolean>(itemData) {
        override fun get(): Boolean? {
            val item = itemData.get()
            return if (item is Enumeration) {
                item.isExcluded
            } else {
                false
            }
        }
    }


    @get:Bindable
    val showIncompleteAndNotes = object: ObservableField<Boolean>(itemData) {
        override fun get(): Boolean? = (itemData.get() as? Enumeration)?.isIncomplete ?: false || 
                !itemData.get()?.note.isNullOrEmpty() ||
                (itemData.get() as? Enumeration)?.isExcluded ?: false
    }


    @get:Bindable
    val showNotePrefix = object : ObservableField<Boolean>(showIncomplete) {
        override fun get(): Boolean? = !(showIncomplete.get()
                ?: false) && !note.get().isNullOrEmpty()
    }


    @get:Bindable
    val distance = object: ObservableField<String>(itemData, currentLocation) {
        override fun get(): String? {
            var distance = ""
            currentLocation.get()?.let {
                val results = FloatArray(3)
                val itemLocation = itemData.get()?.location
                Location.distanceBetween(it.latitude,
                        it.longitude,
                        itemLocation?.latitude ?: 0.0,
                        itemLocation?.longitude ?: 0.0,
                        results)
                
                distance = String.format("%.1f km away", results[0] / 1000)
            }
            
            
            return distance
        }
    }
    
    @get:Bindable
    val note = object : ObservableField<String>(itemData) {
        override fun get() = itemData.get()?.note

    }

    @get:Bindable
    var visibility by bindDelegate(false)

    val gpsPrecisionVm = CollectGpsPrecisionViewModel(userSettings?.gpsMinimumPrecision ?: 0.0,
            userSettings?.gpsPreferredPrecision ?: 0.0, 
            lowestColor, 
            mediumColor, 
            highestColor)
}