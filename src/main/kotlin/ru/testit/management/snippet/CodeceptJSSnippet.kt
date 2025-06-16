package ru.testit.management.snippet

import ru.testit.management.utils.CodeSnippetUtils.getTestName
import ru.testit.management.utils.CodeSnippetUtils.tryUpdateLineWithSteps
import ru.testit.management.utils.StringUtils
import ru.testit.management.windows.tools.TmsNodeModel

object CodeceptJSSnippet {
    private const val CODE_SNIPPET = """        
    Scenario('testName',
    {
        externalId: 'externalId',
        displayName: 'displayName_',
        title: 'title_',
        description: 'description',
        workitemIds: ['globalId']
    },
    ({ I }) => {
        // See work item [globalId] for detailed steps description
        // Pre:
        //   preconditions
        // Steps:
        //   testSteps
        // Post:
        //   postconditions
    });
    """

    val comparator = { globalId: Long  -> "workitemIds: ['$globalId']" }


    fun getNewSnippetCodeceptJS(userObject: Any): String {
        val model = userObject as TmsNodeModel
        val builder = StringBuilder()

        val testName = getTestName(model)
        CODE_SNIPPET.lines().forEach { line ->
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