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
            "<html><body style='width: 350px;'>Expected URL scheme 'http' or 'https' but no scheme was found for /api/v...</body></html>"

        // Act
        val actual = TmsClient.getSettingsValidationErrorMsg("", "", "")

        // Assert
        assertEquals("Validation error message assertion failed!", expected, actual)
    }
}