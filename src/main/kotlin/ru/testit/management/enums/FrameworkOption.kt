package ru.testit.management.enums

enum class FrameworkOption {
    JUNIT {
        override fun toString() = "junit"
    },
    BEHAVE {
        override fun toString() = "behave"
    },
    NOSE {
        override fun toString() = "nose"
    },
    PYTEST {
        override fun toString() = "pytest"
    },
    ROBOTFRAMEWORK {
        override fun toString() = "robotframework"
    },
    MSTEST {
        override fun toString() = "mstest"
    },
    NUNIT {
        override fun toString() = "nunit"
    },
    XUNIT {
        override fun toString() = "xunit"
    },
    SPECFLOW {
        override fun toString() = "specflow"
    },
    CODECEPTJS {
        override fun toString() = "codeceptjs"
    },
    CUCUMBER {
        override fun toString() = "cucumber"
    },
    JEST {
        override fun toString() = "jest"
    },
    MOCHA {
        override fun toString() = "mocha"
    },
    PLAYWRIGHT {
        override fun toString() = "playwright"
    },
    TESTCAFE {
        override fun toString() = "testcafe"
    }
}
