package org.taskforce.episample.config.language

import java.io.Serializable

data class CustomLanguage(val id: String,
                          val name: String,
                          val strings: Map<String, String>,
                          val isAdmin: Boolean = true,
                          val isUser: Boolean = true) : Serializable