package ru.testit.management

import org.junit.jupiter.api.DisplayName
import ru.testit.management.clients.TmsClient
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test


@DisplayName("Client")
class ClientTests {

    @DisplayName("Validation")
    @Test
    fun checkMessage() {
        // Arrange
        val expected =
            "URI is not absolute"

        // Act
        val actual = TmsClient("").getSettingsValidationErrorMsg("", "")

        // Assert
        assertEquals("Validation error message assertion failed!", expected, actual)
    }
}