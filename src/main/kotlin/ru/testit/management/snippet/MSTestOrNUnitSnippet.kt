package ru.testit.management.snippet

import ru.testit.management.utils.CodeSnippetUtils.getTestName
import ru.testit.management.utils.CodeSnippetUtils.tryUpdateLineWithSteps
import ru.testit.management.utils.StringUtils
import ru.testit.management.windows.tools.TmsNodeModel

object MSTestOrNUnitSnippet {
    private const val CODE_SNIPPET = """
    [ExternalId("externalId")]
    [DisplayName("displayName_")]
    [Title("title_")]
    [Tms.Adapter.Attributes.Description("description")]
    [WorkItemIds("globalId")]
    [TestMethod]
    public void testName()
    {
        // See work item [globalId] for detailed steps description
        // Pre:
        //   preconditions
        // Steps:
        //   testSteps
        // Post:
        //   postconditions
    }
    """

    val comparator = { globalId: Long ->
        Regex("""\[WorkItemIds\s*\([^)]*["']$globalId["']""")
    }


    fun getNewSnippetMSTestOrNUnit(userObject: Any): String {
        val model = userObject as TmsNodeModel
        val builder = StringBuilder()

        val testName = getTestName(model)
        val testMethodName = StringUtils.cleanForMethodName(
            StringUtils.spacesToCamelCase(testName))
        CODE_SNIPPET.lines().forEach { line ->
            var modifiedLine = line
                .replace("testName",
                    testMethodName)
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