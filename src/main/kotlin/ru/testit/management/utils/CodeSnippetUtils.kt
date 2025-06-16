package ru.testit.management.utils

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import ru.testit.client.model.StepModel
import ru.testit.management.enums.FrameworkOption
import ru.testit.management.snippet.JunitSnippet
import ru.testit.management.snippet.MSTestOrNUnitSnippet
import ru.testit.management.snippet.XUnitSnippet
import ru.testit.management.snippet.CodeceptJSSnippet
import ru.testit.management.snippet.GherkinSnippet
import ru.testit.management.snippet.MochaSnippet
import ru.testit.management.snippet.PlaywrightOrJestSnippet
import ru.testit.management.snippet.TestCafeSnippet
import ru.testit.management.snippet.PytestOrNoseSnippet
import ru.testit.management.snippet.RobotFrameworkSnippet
import ru.testit.management.windows.settings.TmsSettingsState
import ru.testit.management.windows.tools.TmsNodeModel

object CodeSnippetUtils {

    fun getNewSnippet(userObject: Any): String {
        val framework: String? = TmsSettingsState.instance.getFramework()
        val snippet = when (framework) {
            FrameworkOption.BEHAVE.toString() -> GherkinSnippet.getNewSnippetCucumberOrBehaveOrSpecFlow(userObject)
            FrameworkOption.NOSE.toString() -> PytestOrNoseSnippet.getNewSnippetPytestOrNose(userObject)
            FrameworkOption.PYTEST.toString() -> PytestOrNoseSnippet.getNewSnippetPytestOrNose(userObject)
            FrameworkOption.ROBOTFRAMEWORK.toString() -> RobotFrameworkSnippet.getNewSnippetRobotFramework(userObject)
            FrameworkOption.JUNIT.toString() -> JunitSnippet.getNewSnippetJunit(userObject)
            FrameworkOption.MSTEST.toString() -> MSTestOrNUnitSnippet.getNewSnippetMSTestOrNUnit(userObject)
            FrameworkOption.NUNIT.toString() -> MSTestOrNUnitSnippet.getNewSnippetMSTestOrNUnit(userObject)
            FrameworkOption.XUNIT.toString() -> XUnitSnippet.getNewSnippetXUnit(userObject)
            FrameworkOption.SPECFLOW.toString() -> GherkinSnippet.getNewSnippetCucumberOrBehaveOrSpecFlow(userObject)
            FrameworkOption.CODECEPTJS.toString() -> CodeceptJSSnippet.getNewSnippetCodeceptJS(userObject)
            FrameworkOption.CUCUMBER.toString() -> GherkinSnippet.getNewSnippetCucumberOrBehaveOrSpecFlow(userObject)
            FrameworkOption.JEST.toString() -> PlaywrightOrJestSnippet.getNewSnippetPlaywrightOrJest(userObject)
            FrameworkOption.MOCHA.toString() -> MochaSnippet.getNewSnippetMocha(userObject)
            FrameworkOption.PLAYWRIGHT.toString() -> PlaywrightOrJestSnippet.getNewSnippetPlaywrightOrJest(userObject)
            FrameworkOption.TESTCAFE.toString() -> TestCafeSnippet.getNewSnippetTestCafe(userObject)
            else -> JunitSnippet.getNewSnippetJunit(userObject)
        }
        return snippet
    }

    fun getComparator(): (Long) -> String {
        val framework: String? = TmsSettingsState.instance.getFramework()
        val comparator = when (framework) {
            FrameworkOption.BEHAVE.toString() -> GherkinSnippet.comparator
            FrameworkOption.NOSE.toString() -> PytestOrNoseSnippet.comparator
            FrameworkOption.PYTEST.toString() -> PytestOrNoseSnippet.comparator
            FrameworkOption.ROBOTFRAMEWORK.toString() -> RobotFrameworkSnippet.comparator
            FrameworkOption.JUNIT.toString() -> JunitSnippet.comparator
            FrameworkOption.MSTEST.toString() -> MSTestOrNUnitSnippet.comparator
            FrameworkOption.NUNIT.toString() -> MSTestOrNUnitSnippet.comparator
            FrameworkOption.XUNIT.toString() -> XUnitSnippet.comparator
            FrameworkOption.SPECFLOW.toString() -> GherkinSnippet.comparator
            FrameworkOption.CODECEPTJS.toString() -> CodeceptJSSnippet.comparator
            FrameworkOption.CUCUMBER.toString() -> GherkinSnippet.comparator
            FrameworkOption.JEST.toString() -> PlaywrightOrJestSnippet.comparator
            FrameworkOption.MOCHA.toString() -> MochaSnippet.comparator
            FrameworkOption.PLAYWRIGHT.toString() -> PlaywrightOrJestSnippet.comparator
            FrameworkOption.TESTCAFE.toString() -> TestCafeSnippet.comparator
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