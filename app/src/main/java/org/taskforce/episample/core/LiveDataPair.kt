package org.taskforce.episample.core

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

class LiveDataPair<A, B>(liveA: LiveData<A>,
                         liveB: LiveData<B>): MediatorLiveData<Pair<A?, B?>>() {
    init {
        addSource(liveA) {
            value = Pair(it, liveB.value)
        }
        addSource(liveB) {
            value = Pair(liveA.value, it)
        }
    }
}