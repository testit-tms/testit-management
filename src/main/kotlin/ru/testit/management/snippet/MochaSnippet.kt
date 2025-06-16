package ru.testit.management.snippet

import ru.testit.management.utils.CodeSnippetUtils.getTestName
import ru.testit.management.utils.CodeSnippetUtils.tryUpdateLineWithSteps
import ru.testit.management.windows.tools.TmsNodeModel

object MochaSnippet {
    private const val CODE_SNIPPET = """     
    it("testName", function () {
        this.externalId = "externalId";
        this.displayName = "displayName_";
        this.title = "title_";
        this.description = "description";
        this.workItemsIds = ["globalId"];
        
        // See work item [globalId] for detailed steps description
        // Pre:
        //   preconditions
        // Steps:
        //   testSteps
        // Post:
        //   postconditions
    });
    """

    val comparator = { globalId: Long  -> "this.workItemsIds = [\"$globalId\"];" }


    fun getNewSnippetMocha(userObject: Any): String {
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