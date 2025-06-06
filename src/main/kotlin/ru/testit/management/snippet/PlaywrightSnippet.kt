package ru.testit.management.snippet

import ru.testit.management.utils.CodeSnippetUtils.getTestName
import ru.testit.management.utils.CodeSnippetUtils.tryUpdateLineWithSteps
import ru.testit.management.utils.StringUtils
import ru.testit.management.windows.tools.TmsNodeModel

object PlaywrightSnippet {
    private const val PLAYWRIGHT_CODE_SNIPPET = """  
    test('testName', () => {
        testit.externalId('externalId');
        testit.displayName('displayName_');
        testit.title('title_');
        testit.description('description');
        testit.workItemIds(["globalId"]);
        
        // See work item [globalId] for detailed steps description
        // Pre:
        //   preconditions
        // Steps:
        //   testSteps
        // Post:
        //   postconditions
    });
    """

    val comparator = { globalId: Long  -> "testit.workItemIds([\"$globalId\"]);" }


    fun getNewSnippetPlaywright(userObject: Any): String {
        val model = userObject as TmsNodeModel
        val builder = StringBuilder()

        val testName = getTestName(model)
        PLAYWRIGHT_CODE_SNIPPET.lines().forEach { line ->
            var modifiedLine = line
                .replace("testName", testName)
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