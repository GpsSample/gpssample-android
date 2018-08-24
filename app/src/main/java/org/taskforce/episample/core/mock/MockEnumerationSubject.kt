package org.taskforce.episample.core.mock

import org.taskforce.episample.core.interfaces.EnumerationSubject

data class MockEnumerationSubject(override val singular: String,
                                  override val plural: String,
                                  override val primaryLabel: String): EnumerationSubject {
    companion object {
        fun createMockEnumerationSubject(singular: String = "Household",
                                         plural: String = "Households",
                                         primaryLabel: String = "Head of Household"): MockEnumerationSubject {
            return MockEnumerationSubject(singular, plural, primaryLabel)
        }
    }
}