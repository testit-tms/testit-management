package ru.testit.management.enums

enum class FrameworkOption {
    JUNIT {
        override fun toString() = "junit"
    },
    PYTEST {
        override fun toString() = "pytest"
    }


}
