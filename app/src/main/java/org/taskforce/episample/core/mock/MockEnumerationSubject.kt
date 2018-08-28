package org.taskforce.episample.core.mock

import org.taskforce.episample.core.interfaces.EnumerationSubject

data class MockEnumerationSubject(override val singular: String,
                                  override val plural: String,
                                  override val primaryLabel: String): EnumerationSubject {
    companion object {
        fun createMockEnumerationSubject(singular: String = "Person",
                                         plural: String = "People",
                                         primaryLabel: String = "First name"): MockEnumerationSubject {
            return MockEnumerationSubject(singular, plural, primaryLabel)
        }
    }
}