package ru.testit.management.snippet

import ru.testit.management.utils.CodeSnippetUtils.getTestName
import ru.testit.management.utils.CodeSnippetUtils.tryUpdateLineWithSteps
import ru.testit.management.utils.StringUtils
import ru.testit.management.windows.tools.TmsNodeModel

object PytestOrNoseSnippet {
    private const val CODE_SNIPPET = """
    @testit.externalId("externalId")
    @testit.displayName("displayName_")
    @testit.title("title_")
    @testit.description("description")
    @testit.workItemIds("globalId")
    def test_testName():
        # See work item [globalId] for detailed steps description
        # Pre:
        #   preconditions
        # Steps:
        #   testSteps
        # Post:
        #   postconditions
    
    """

    val comparator = { globalId: Long  -> "@testit.workItemIds(\"$globalId\")" }


    fun getNewSnippetPytestOrNose(userObject: Any): String {
        val model = userObject as TmsNodeModel
        val builder = StringBuilder()

        val testName = getTestName(model)
        CODE_SNIPPET.lines().forEach { line ->
            var modifiedLine = line
                .replace("testName",
                    StringUtils.spacesToSnakeCase(testName).lowercase())
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