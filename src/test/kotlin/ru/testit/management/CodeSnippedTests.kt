package ru.testit.management

import kotlinx.collections.immutable.persistentSetOf
import org.junit.jupiter.api.DisplayName
import ru.testit.kotlin.client.models.StepModel
import ru.testit.management.snippet.JunitSnippet
import ru.testit.management.utils.CodeSnippetUtils
import ru.testit.management.windows.tools.TmsNodeModel
import java.util.*
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test


@DisplayName("Code snippet")
class CodeSnippedTests {

    @DisplayName("Create")
    @Test
    fun checkCodeSnippet() {
        // Arrange
        val name = "Test name"
        val globalID = 12345L

        val preModel = StepModel(UUID.randomUUID(), action = "Precondition text")
        val pre = persistentSetOf(preModel)

        val stepModel = StepModel(UUID.randomUUID(), action = "Step text")
        val steps = persistentSetOf(stepModel)

        val postModel = StepModel(UUID.randomUUID(), action = "Postcondition text")
        val post = persistentSetOf(postModel)

        val model = TmsNodeModel(name, globalID, pre, steps, post)
        val expected = """@WorkItemIds("$globalID")
@Test
public void testName() {
    // See work item [$globalID] for detailed steps description
    // Pre:
    //   ${preModel.action}
    // Steps:
    //   ${stepModel.action}
    // Post:
    //   ${postModel.action}
}"""

        // Act
        val actual = JunitSnippet.getNewSnippetJunit(model)

        // Assert
        assertEquals("Code snipped text assertion failed!", expected, actual)
    }
}