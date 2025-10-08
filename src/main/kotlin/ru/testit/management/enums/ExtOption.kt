package ru.testit.management.enums

enum class ExtOption {
    JAVA {
        override fun toString() = "java"
    },
    PYTHON {
        override fun toString() = "py"
    },
    ROBOT {
        override fun toString() = "robot"
    },
    CSHARP {
        override fun toString() = "cs"
    },
    TYPESCRIPT {
        override fun toString() = "ts"
    },
    GHERKIN {
        override fun toString() = "feature"
    }
}
