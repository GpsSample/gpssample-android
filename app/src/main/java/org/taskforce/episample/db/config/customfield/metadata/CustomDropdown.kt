package org.taskforce.episample.db.config.customfield.metadata

import org.taskforce.episample.core.interfaces.CustomDropdown
import java.util.*

class CustomDropdown(override val value: String?,
                     override val key: String = UUID.randomUUID().toString()): CustomDropdown