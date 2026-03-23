package ru.testit.management.utils

import java.util.*

object StringUtils {

    val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    val snakeRegex = "_[a-zA-Z]".toRegex()

    // String extensions
    private fun camelToSnakeCase(str: String): String {
        return camelRegex.replace(str) {
                "_${it.value}"
            }.lowercase(Locale.getDefault())
    }

    private fun snakeToLowerCamelCase(str: String): String {
        return snakeRegex.replace(str) {
            it.value.replace("_","")
                .uppercase(Locale.getDefault())
        }.replaceFirstChar { if (it.isUpperCase())
                it.lowercase(Locale.getDefault())
            else it.toString() }
    }

    private fun snakeToUpperCamelCase(str: String): String {
        return this.snakeToLowerCamelCase(str)
            .replaceFirstChar { if (it.isLowerCase())
                it.titlecase(Locale.getDefault())
            else it.toString() }
    }

    fun spacesToSnakeCase(str: String): String {
        return str.replace(" ", "_")
    }

    fun spacesToCamelCase(str: String): String {
        return snakeToLowerCamelCase(spacesToSnakeCase(str))
    }

    fun textToLong(text: String?): Long? {
        val trimmed = text?.trim().orEmpty()

        if (trimmed.isEmpty()) return null

        return trimmed.toLongOrNull()
    }

    fun textToBool(selected: String?): Boolean? {
        return when (selected) {
            "True" -> true
            "False" -> false
            else -> null
        }
    }

    fun cleanForMethodName(str: String): String {
        return forbiddenCharactersForMethodName.replace(str) { "" }
    }
}
