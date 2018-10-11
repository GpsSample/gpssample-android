package org.taskforce.episample.collection.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.location.Location
import org.taskforce.episample.EpiApplication
import org.taskforce.episample.collection.viewmodels.CollectDetailField
import org.taskforce.episample.core.LiveDataPair
import org.taskforce.episample.core.interfaces.*
import org.taskforce.episample.db.config.customfield.CustomFieldType
import org.taskforce.episample.db.config.customfield.value.*
import org.taskforce.episample.utils.DateUtil
import javax.inject.Inject

class CollectDetailsViewModel(application: Application): AndroidViewModel(application) {
    
    @Inject
    lateinit var config: Config
    
    @Inject
    lateinit var locationService: LocationService
    
    @Inject
    lateinit var collectManager: CollectManager
    
    init {
        (application as EpiApplication).collectComponent?.inject(this)
        
        locationService.collectBreadcrumbs = false
    }
    
    var data = MutableLiveData<CollectItem>()
    
    val showPhotoCard = Transformations.map(data) {
        !it.image.isNullOrBlank()
    }
    
    val showExcluded = Transformations.map(data) { collectItem ->
        when (collectItem) {
            is Enumeration -> collectItem.isExcluded
            else -> false
        }
    }
    
    val showIncomplete = Transformations.map(data) { collectItem ->
        when (collectItem) {
            is Enumeration -> collectItem.isIncomplete
            else -> false
        }
    }
    
    val photoSource = Transformations.map(data) {
        it.image
    }
    
    private val locationPair = LiveDataPair(data, locationService.locationLiveData)
    val distanceText: LiveData<String> = Transformations.map(locationPair) {
        val currentLocation = it.second.first
        val distanceArray = FloatArray(3)
        val itemLocation = it.first.location
        
        Location.distanceBetween(currentLocation.latitude,
                currentLocation.longitude,
                itemLocation.latitude,
                itemLocation.longitude,
                distanceArray)
        
        val kilometers = distanceArray[0] / 1000
        val meters = distanceArray[0]
        
        var distance = kilometers
        var unit = "km"
        if (meters < 1000) {
            distance = meters
            unit = "m"
        }
        
        String.format("%.1f $unit away", distance)
    }
    
    val gpsDisplay = Transformations.map(data) {
        val latLng = it.location
        "%.5f ".format(latLng.latitude) + ", %.5f".format(latLng.longitude)
    }
    
    val customFields: LiveData<List<CollectDetailField>> = Transformations.map(data) { collectItem ->
        when (collectItem) {
            is Enumeration -> {
                val fields: MutableList<CollectDetailField> = mutableListOf()
                
                collectItem.title?.let { 
                    var title = it
                    val isIncomplete = title.isBlank()
                    if (isIncomplete) {
                        title = "Empty"
                    }
                    val titleField = CollectDetailField(collectItem, "${config.enumerationSubject.primaryLabel}:", title, isIncomplete = isIncomplete)
                    fields.add(titleField)
                }
                
                config.customFields.forEach { customField ->
                    val customFieldValue = collectItem.customFieldValues.firstOrNull { 
                        it.customFieldId == customField.id
                    }
                    
                    var isCheckbox = when (customField.type) {
                        CustomFieldType.CHECKBOX -> true
                        else -> false
                    }
                    
                    if (!customField.isAutomatic) {
                        var value = getValueForCustomField(customFieldValue, customField)
                        val isIncomplete = customField.isRequired && collectItem.isIncomplete && value.isBlank()
                        
                        if (value.isBlank()) {
                            value = "Empty"
                        }
                        
                        if (isCheckbox && value == "Empty") {
                            value = "Unchecked"
                            isCheckbox = false
                        }
                        
                        val field = CollectDetailField(collectItem, "${customField.name}:", value, isCheckbox, isIncomplete)
                        fields.add(field)
                    }
                }
                
                collectItem.note?.let { note ->
                    fields.add(CollectDetailField(collectItem, "Notes:", note)) // TODO: Translate the word "Note"
                }
                
                return@map fields
            }
            is Landmark -> {
                val name = CollectDetailField(collectItem, "Name:", collectItem.title)
                
                val type = CollectDetailField(collectItem, "Landmark Type:", collectItem.landmarkType.name, showLandmarkType = true)
                
                val notes = CollectDetailField(collectItem, "Notes:", collectItem.note ?: "None")
                
                return@map listOf(name, type, notes)
            }
            else -> emptyList<CollectDetailField>()
        }
    }
    
    private fun getValueForCustomField(customFieldValue: CustomFieldValue?, customField: CustomField): String {
        val cfv = customFieldValue?.value
        return when (cfv) {
            is BooleanValue -> {
                cfv.boolValue.toString()
            }
            is DateValue -> {
                val date = cfv.dateValue
                DateUtil.getFormattedDate(date, config.displaySettings)
            }
            is DoubleValue -> {
                cfv.doubleValue.toString()
            }
            is IntValue -> {
                cfv.intValue.toString()
            }
            is TextValue -> {
                cfv.text
            }
            is DropdownValue -> {
                val dropdownMetadata = customField.metadata as org.taskforce.episample.db.config.customfield.metadata.DropdownMetadata
                val dropdownItem = dropdownMetadata.items.first { it.key == cfv.customDropdownId }
                dropdownItem.value ?: ""
            }
            else -> {
                ""
            }
        }
    }
    
    val gpsBreadcrumbs = collectManager.getBreadcrumbs()
    
    val collectItems = collectManager.getCollectItems()
}