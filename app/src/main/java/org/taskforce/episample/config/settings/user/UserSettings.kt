package org.taskforce.episample.config.settings.user

import java.io.Serializable

class UserSettings(val gpsMinimumPrecision: Double,
                   val gpsPreferredPrecision: Double,
                   val allowPhotos: Boolean,
                   val photoCompressionScale: Int?,
                   val requireComments: Boolean,
                   val enforceMinimumDistance: Boolean,
                   val minimumDistance: Int?,
                   val supervisorCanDisablePhotos: Boolean,
                   val requireSupervisorPassword: Boolean,
                   val supervisorPassword: String?) : Serializable