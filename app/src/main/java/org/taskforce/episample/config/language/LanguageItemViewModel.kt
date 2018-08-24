package org.taskforce.episample.config.language

class LanguageItemViewModel(var checked: Boolean,
                            val customLanguage: CustomLanguage,
                            val admin: String,
                            val user: String,
                            val onClick: () -> Unit) {

    fun onClick() {
        checked = !checked
        onClick.invoke()
    }
}