package org.taskforce.episample.core.interfaces

interface CustomLanguage {
    val id: String
    val name: String
    val strings: Map<String, String>
    val isAdmin: Boolean
    val isUser: Boolean
}

interface BuiltInLanguage : CustomLanguage


data class LiveCustomLanguage(override val id: String,
                              override val name: String,
                              override val strings: Map<String, String>,
                              override val isAdmin: Boolean = false,
                              override val isUser: Boolean = true): CustomLanguage

data class LiveBuiltInLanguage(override val id: String,
                               override val name: String,
                               override val strings: Map<String, String>,
                               override val isAdmin: Boolean = false,
                               override val isUser: Boolean = true): BuiltInLanguage