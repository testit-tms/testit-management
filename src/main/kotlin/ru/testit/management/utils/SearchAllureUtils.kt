package ru.testit.management.utils

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.testit.management.parsers.models.MatchInfo
import ru.testit.management.windows.tools.TmsToolWindow
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

object SearchAllureUtils {
    private var _isResearching: Boolean = false
    private val _logger = Logger.getLogger(SyncUtils::class.java.simpleName)

    fun research() {
        synchronized(TmsToolWindow.instance) {
            if (_isResearching) {
                return
            }

            _isResearching = true
        }

        ApplicationManager.getApplication().runReadAction {
            try {
                val project = DataManager
                    .getInstance()
                    .dataContextFromFocusAsync
                    .then { PlatformDataKeys.PROJECT.getData(it) }
                    .blockingGet(3, TimeUnit.SECONDS)

                if (project != null) {
                    val regexPatterns = ParsingAnnotationsUtils.getAllPatterns()
                    if (regexPatterns.isEmpty()) return@runReadAction
                    val currentResults = getResults(project, regexPatterns)

                    TmsToolWindow.instance.research(project, currentResults)
                }
            } catch (exception: Throwable) {
                _logger.severe { exception.message }
            } finally {
                _isResearching = false
            }
        }
    }

    private fun getResults(project: Project, regexPatterns: List<Regex>): MutableMap<String, List<MatchInfo>> {
        val results = mutableMapOf<String, List<MatchInfo>>()

        for (regexPattern in regexPatterns) {
            val roots = project.getBaseDirectories()

            for (root in roots) {
                val matches = searchFiles(project, root, regexPattern)

                for (matchKey in matches.keys) {
                    if (results.containsKey(matchKey)) {
                        val joinedList = mutableListOf<MatchInfo>()
                        joinedList.addAll(results[matchKey]!!)
                        joinedList.addAll(matches[matchKey]!!)
                        results[matchKey] = joinedList
                        continue
                    }

                    results[matchKey] = matches[matchKey]!!
                }
            }
        }

        return results
    }

    private fun searchFiles(project: Project, file: VirtualFile, pattern: Regex): MutableMap<String, List<MatchInfo>> {
        val results = mutableMapOf<String, List<MatchInfo>>()

        if (file.isDirectory) {
            file.children.forEach { child ->
                results.putAll(
                    searchFiles(project, child, pattern)
                )
            }
        } else {
            try {
                val content = FileDocumentManager.getInstance().getDocument(file)?.text
                val matches = pattern.findAll(content.toString()).map { match ->
                    val (lineNumber, column) = calculateLineAndColumn(content.toString(), match.range.first)
                    MatchInfo(
                        text = match.value,
                        start = match.range.first,
                        end = match.range.last + 1,
                        lineNumber = lineNumber,
                        filePath = file.path,
                        column = column
                    )
                }.toList()

                if (matches.isNotEmpty()) {
                    results[file.path] = matches
                }
            } catch (e: Exception) {
                println("Error processing file ${file.path}: ${e.message}")
            }
        }

        return results
    }

    private fun calculateLineAndColumn(content: String, position: Int): Pair<Int, Int> {
        if (position < 0 || position > content.length) {
            return Pair(-1, -1)
        }

        val textBefore = content.substring(0, position)
        val lineNumber = textBefore.count { it == '\n' }
        val lastNewLine = textBefore.lastIndexOf('\n')
        val column = if (lastNewLine == -1) position + 1 else position - lastNewLine

        return Pair(lineNumber, column)
    }
}