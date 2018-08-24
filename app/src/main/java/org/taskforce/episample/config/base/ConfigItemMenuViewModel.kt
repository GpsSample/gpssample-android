package org.taskforce.episample.config.base

import org.taskforce.episample.db.config.Config

class ConfigItemMenuViewModel(
        val imageRes: Int,
        val text: String,
        val action: (Config) -> Unit)