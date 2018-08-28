package org.taskforce.episample.core.interfaces

interface UserSession {
    val username: String
    val isSupervisor: Boolean

    val configId: String
    val studyId: String
}

data class LiveUserSession(override val username: String,
                           override val isSupervisor: Boolean,
                           override val configId: String,
                           override val studyId: String) : UserSession