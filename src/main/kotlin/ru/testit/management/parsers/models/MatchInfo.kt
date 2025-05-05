package ru.testit.management.parsers.models

data class MatchInfo(
    val text: String,
    val start: Int,
    val end: Int,
    val lineNumber: Int,
    val column: Int,
    val filePath: String,
)
