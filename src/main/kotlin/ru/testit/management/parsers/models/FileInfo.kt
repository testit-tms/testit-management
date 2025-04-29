package ru.testit.management.parsers.models

data class FileInfo(
    val filePath: String,
    val oldText: String,
    val newText: String,
    val matches: List<MatchInfo>,
    val replacements: List<ReplacementInfo>,
)
