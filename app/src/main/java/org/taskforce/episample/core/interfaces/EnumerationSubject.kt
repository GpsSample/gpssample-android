package org.taskforce.episample.core.interfaces

import java.io.Serializable

interface EnumerationSubject {
    val singular: String
    val plural: String
    val primaryLabel: String
}

data class LiveEnumerationSubject(override val singular: String,
                                  override val plural: String,
                                  override val primaryLabel: String): EnumerationSubject, Serializable