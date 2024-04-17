package ru.testit.management.utils

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import ru.testit.client.model.StepModel
import ru.testit.management.windows.tools.TmsNodeModel

object CodeSnippedUtils {
    private const val DEFAULT_CODE_SNIPPET = """
    @WorkItemIds("globalId")
    @Test
    public void testName() {
        // See work item [globalId] for detailed steps description
        // Pre:
        //   preconditions
        // Steps:
        //   testSteps
        // Post:
        //   postconditions
    }
"""

    fun getNewSnippet(userObject: Any): String {
        val model = userObject as TmsNodeModel
        val builder = StringBuilder()

        DEFAULT_CODE_SNIPPET.lines().forEach { line ->
            var modifiedLine = line
                .replace("testName", getTestName(model))
                .replace("globalId", model.globalId.toString())

            modifiedLine = tryUpdateLineWithSteps(modifiedLine, model)

            if (modifiedLine.isNotBlank()) {
                builder.appendLine(modifiedLine)
            }
        }

        return builder.toString().trimIndent()
    }

    private fun getTestName(model: TmsNodeModel): String {
        var testName = model.name.orEmpty()

        while (testName.isNotBlank() && testName[0].isDigit()) {
            testName = testName.drop(1)
        }

        val builder = StringBuilder()
        var lastCharSkipped = false

        for (symbol in testName) {
            if (symbol.isLetterOrDigit()) {
                when {
                    builder.isBlank() -> {
                        builder.append(symbol.lowercaseChar())
                    }

                    lastCharSkipped && builder.isNotBlank() -> {
                        builder.append(symbol.uppercaseChar())
                    }

                    else -> {
                        builder.append(symbol)
                    }
                }

                lastCharSkipped = false
            } else {
                lastCharSkipped = true
            }
        }

        return builder.toString()
    }

    private fun tryUpdateLineWithSteps(line: String, model: TmsNodeModel): String {
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