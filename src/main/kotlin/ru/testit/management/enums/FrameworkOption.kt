package ru.testit.management.enums

enum class FrameworkOption {
    JUNIT {
        override fun toString() = "junit"
    },
    PYTEST {
        override fun toString() = "pytest"
    },
    MSTEST {
        override fun toString() = "mstest"
    },
    PLAYWRIGHT {
        override fun toString() = "playwright"
    }
}
