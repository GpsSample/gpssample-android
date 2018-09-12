package org.taskforce.episample.core

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

class LiveDataPair<A, B>(liveA: LiveData<A>,
                         liveB: LiveData<B>) : MediatorLiveData<Pair<A, B>>() {

    private var hasA: Boolean = false
    private var hasB: Boolean = false

    private val merge = {
        if (hasA && hasB) {
            liveA.value?.let { unwrappedA ->
                liveB.value?.let { unwrappedB ->
                    postValue(Pair(unwrappedA, unwrappedB))
                }
            }
        }
    }

    init {
        addSource(liveA) {
            hasA = true
            merge()
        }
        addSource(liveB) {
            hasB = true
            merge()
        }
    }
}