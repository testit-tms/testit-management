package ru.testit.management.snippet

import ru.testit.management.utils.CodeSnippetUtils
import ru.testit.management.utils.CodeSnippetUtils.getTestName
import ru.testit.management.utils.CodeSnippetUtils.tryUpdateLineWithSteps
import ru.testit.management.utils.StringUtils
import ru.testit.management.windows.tools.TmsNodeModel

object RobotFrameworkSnippet {
    private const val CODE_SNIPPET = """  
    testName
        [Tags]  testit.externalID:externalId
        ...     testit.displayName:displayName_
        ...     testit.title:title_
        ...     testit.description:description
        ...     testit.workitemsID:globalId
        # See work item [globalId] for detailed steps description
        # Pre:
        #   preconditions
        # Steps:
        #   testSteps
        # Post:
        #   postconditions
    """

    val comparator = { globalId: Long  -> "testit.workitemsID:$globalId" }


    fun getNewSnippetRobotFramework(userObject: Any): String {
        val model = userObject as TmsNodeModel
        val builder = StringBuilder()

        val testName = getTestName(model)
        CODE_SNIPPET.lines().forEach { line ->
            var modifiedLine = line
                .replace("testName", testName)
                .replace("globalId", model.globalId.toString())
                .replace("title_",
                    StringUtils.spacesToSnakeCase(testName))
                .replace("displayName_",
                    StringUtils.spacesToSnakeCase(testName))

            modifiedLine = tryUpdateLineWithSteps(modifiedLine, model)

            if (modifiedLine.isNotBlank()) {
                builder.appendLine(modifiedLine)
            }
        }

        return builder.toString().trimIndent()
    }
}