package ru.testit.management.snippet

import ru.testit.management.utils.CodeSnippetUtils.getTestName
import ru.testit.management.utils.CodeSnippetUtils.tryUpdateLineWithSteps
import ru.testit.management.utils.StringUtils
import ru.testit.management.windows.tools.TmsNodeModel

object GherkinSnippet {
    private const val CODE_SNIPPET = """
    @ExternalId=externalId
    @DisplayName=displayName_
    @Title=title_
    @Description=description
    @WorkItemIds=globalId
    Scenario: testName
        # See work item [globalId] for detailed steps description
        # Pre:
        #   preconditions
        # Steps:
        #   testSteps
        # Post:
        #   postconditions
    """

    val comparator = { globalId: Long  -> "@WorkItemIds=$globalId" }


    fun getNewSnippetCucumberOrBehaveOrSpecFlow(userObject: Any): String {
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