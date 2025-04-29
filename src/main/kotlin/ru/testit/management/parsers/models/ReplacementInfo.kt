package ru.testit.management.parsers.models

data class ReplacementInfo(
    val text: String,
    val start: Int,
    val end: Int,
    val filePath: String,
)
