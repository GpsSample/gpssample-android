package org.taskforce.episample.main

class MainItemViewModel(val iconRes: Int,
                        val title: String,
                        val description: String,
                        val visible: Boolean,
                        val onClick: () -> Unit)
