package ru.testit.management

import org.junit.jupiter.api.DisplayName
import ru.testit.management.utils.MessagesUtils
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test


@DisplayName("Messages")
class MessagesTests {

    @DisplayName("Copy")
    @Test
    fun checkCopy() {
        // Arrange
        val expected = "Copy"

        // Act
        val actual = MessagesUtils.get("window.tool.popup.copy.text")

        // Assert
        assertEquals("Message text assertion failed!", expected, actual)
    }
}