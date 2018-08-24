package org.taskforce.episample.permissions.models

data class PermissionItem(
        var hasPermission: Boolean,
        val title: String,
        val text: String)