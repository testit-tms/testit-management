package ru.testit.management.snippet

import ru.testit.management.utils.CodeSnippetUtils.getTestName
import ru.testit.management.utils.CodeSnippetUtils.tryUpdateLineWithSteps
import ru.testit.management.utils.StringUtils
import ru.testit.management.windows.tools.TmsNodeModel

object JunitSnippet {
    const val JUNIT_CODE_SNIPPET = """
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


    val comparator = { globalId: Long  -> "@WorkItemIds(\"$globalId\")" }


    fun getNewSnippetJunit(userObject: Any): String {
        val model = userObject as TmsNodeModel
        val builder = StringBuilder()

        JUNIT_CODE_SNIPPET.lines().forEach { line ->
            var modifiedLine = line
                .replace("testName",
                    StringUtils.spacesToCamelCase(getTestName(model)))
                .replace("globalId", model.globalId.toString())

            modifiedLine = tryUpdateLineWithSteps(modifiedLine, model)

            if (modifiedLine.isNotBlank()) {
                builder.appendLine(modifiedLine)
            }
        }

        return builder.toString().trimIndent()
    }

}