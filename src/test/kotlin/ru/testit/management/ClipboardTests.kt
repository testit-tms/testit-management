package ru.testit.management

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import ru.testit.management.utils.ClipboardUtils
import java.awt.Toolkit.getDefaultToolkit
import java.awt.datatransfer.DataFlavor
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test


@DisplayName("Clipboard")
class ClipboardTests {

    @DisplayName("Copy")
    @DisabledIfSystemProperty(named = "TEST_CI", matches = "true")
    @Test
    fun checkCopy() {
        // Arrange
        val expected = "Test123"

        // Act
        ClipboardUtils.copyToClipboard(expected)
        val actual = getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String

        // Assert
        assertEquals("Clipboard value assertion failed!", expected, actual)
    }
}