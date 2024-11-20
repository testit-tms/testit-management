package ru.testit.management.utils

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import ru.testit.client.model.StepModel
import ru.testit.management.enums.FrameworkOption
import ru.testit.management.snippet.JunitSnippet
import ru.testit.management.snippet.PytestSnippet
import ru.testit.management.windows.settings.TmsSettingsState
import ru.testit.management.windows.tools.TmsNodeModel

object CodeSnippetUtils {

    fun getNewSnippet(userObject: Any): String {
        val framework: String? = TmsSettingsState.instance.getFramework()
        val snippet = when (framework) {
            FrameworkOption.PYTEST.toString() -> PytestSnippet.getNewSnippetPytest(userObject)
            FrameworkOption.JUNIT.toString() -> JunitSnippet.getNewSnippetJunit(userObject)
            else -> JunitSnippet.getNewSnippetJunit(userObject)
        }
        return snippet
    }

    fun getComparator(): (Long) -> String {
        val framework: String? = TmsSettingsState.instance.getFramework()
        val comparator = when (framework) {
            FrameworkOption.PYTEST.toString() -> PytestSnippet.comparator
            FrameworkOption.JUNIT.toString() -> JunitSnippet.comparator
            else -> JunitSnippet.comparator
        }
        return comparator
    }

    fun getTestName(model: TmsNodeModel): String {
        var testName = model.name.orEmpty()

        while (testName.isNotBlank() && testName[0].isDigit()) {
            testName = testName.drop(1)
        }
        return testName
    }

    fun tryUpdateLineWithSteps(line: String, model: TmsNodeModel): String {
        when {
            line.contains("preconditions") -> {
                val prefix = line.substringBefore("preconditions")
                return getNewLineWithStepsInserted(prefix, model.preconditions)
            }

            line.contains("testSteps") -> {
                val prefix = line.substringBefore("testSteps")
                return getNewLineWithStepsInserted(prefix, model.steps)
            }

            line.contains("postconditions") -> {
                val prefix = line.substringBefore("postconditions")
                return getNewLineWithStepsInserted(prefix, model.postconditions)
            }

            else -> {
                return line
            }
        }
    }

    private fun getNewLineWithStepsInserted(prefix: String, steps: Iterable<StepModel>?): String {
        val builder = StringBuilder()

        steps?.forEach { step ->
            val stepName = toHumanReadableStepName(prefix, step)

            if (stepName.isNotBlank()) {
                builder.appendLine(stepName)
            }
        }

        return builder.lines().dropLast(1).joinToString(System.lineSeparator())
    }

    private fun toHumanReadableStepName(prefix: String, step: StepModel): String {
        var stepName = if (step.workItem == null) {
            step.action.orEmpty()
        } else {
            step.workItem?.name.orEmpty()
        }

        stepName = Jsoup.clean(stepName, Safelist.none()).lines().joinToString(" ")

        return StringBuilder()
            .append(prefix)
            .append(stepName)
            .toString()
    }
}