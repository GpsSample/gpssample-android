package org.taskforce.episample.config.settings.server

import java.io.Serializable

data class ServerSettings(
        val serverType: String,
        val url: String,
        val username: String,
        val password: String) : Serializable