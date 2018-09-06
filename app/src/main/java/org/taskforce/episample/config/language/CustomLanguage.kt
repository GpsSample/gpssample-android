package org.taskforce.episample.config.language

import java.io.Serializable

data class CustomLanguage(override val id: String,
                          override val name: String,
                          override val strings: Map<String, String>,
                          override val isAdmin: Boolean = true,
                          override val isUser: Boolean = true) : Serializable, org.taskforce.episample.core.interfaces.CustomLanguage