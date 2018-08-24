package org.taskforce.episample.config.geography

class EnumerationLayer(val name: String,
                       var enumerationLayers: MutableList<EnumerationLayer>? = null,
                       var enumerationAreas: MutableList<EnumerationArea>? = null) {
    val elementCount: Int
        get() = (enumerationLayers
                ?.map { it.elementCount }
                ?.reduce { acc, i -> acc + i } ?: 0) +
                (enumerationAreas?.size ?: 0)

    /**
     * @param enumerationArea is the enumeration area to be removed
     * @return Returns Boolean representing whether or not an element was returned
     */
    fun remove(enumerationArea: EnumerationArea): Boolean {
        if (enumerationAreas?.contains(enumerationArea) == true) {
            enumerationAreas?.remove(enumerationArea)
            return true
        }

        //TODO this method should be inspected for correctness based on the implementation of the EnumerationLayer feature.
        enumerationLayers?.forEach {
            if (it.remove(enumerationArea)) {
                return true
            }
        }

        return false
    }
}