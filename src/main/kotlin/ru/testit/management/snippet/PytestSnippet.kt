package ru.testit.management.snippet

import ru.testit.management.utils.CodeSnippetUtils
import ru.testit.management.utils.CodeSnippetUtils.getTestName
import ru.testit.management.utils.CodeSnippetUtils.tryUpdateLineWithSteps
import ru.testit.management.utils.StringUtils
import ru.testit.management.windows.tools.TmsNodeModel

object PytestSnippet {
    const val PYTEST_CODE_SNIPPET = """
    @testit.externalId("externalId")
    @testit.displayName("displayName_")
    @testit.title("title_")
    @testit.description("description")
    @testit.workItemIds("globalId")
    def testName(): 
        # See work item [globalId] for detailed steps description
        # Pre:
        #   preconditions
        # Steps:
        #   testSteps
        # Post:
        #   postconditions
    
"""

    val comparator = { globalId: Long  -> "@testit.workItemIds(\"$globalId\")" }


    fun getNewSnippetPytest(userObject: Any): String {
        val model = userObject as TmsNodeModel
        val builder = StringBuilder()

        val testName = getTestName(model)
        PYTEST_CODE_SNIPPET.lines().forEach { line ->
            var modifiedLine = line
                .replace("testName",
                    StringUtils.spacesToSnakeCase(testName))
                .replace("globalId", model.globalId.toString())
                .replace("title_", testName)
                .replace("displayName_", testName)

            modifiedLine = tryUpdateLineWithSteps(modifiedLine, model)

            if (modifiedLine.isNotBlank()) {
                builder.appendLine(modifiedLine)
            }
        }

        return builder.toString().trimIndent()
    }
}