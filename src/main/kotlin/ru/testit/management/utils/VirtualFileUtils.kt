package ru.testit.management.utils

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import ru.testit.management.enums.ExtOption
import ru.testit.management.enums.FrameworkOption
import ru.testit.management.parsers.models.FileInfo
import ru.testit.management.parsers.models.MatchInfo
import ru.testit.management.parsers.models.ReplacementInfo
import ru.testit.management.windows.settings.TmsSettingsState
import ru.testit.management.windows.tools.CheckBoxNode
import ru.testit.management.windows.tools.TmsNodeModel
import javax.swing.tree.DefaultMutableTreeNode

object VirtualFileUtils {
    private val projectFiles = mutableSetOf<VirtualFile>()

    fun refreshFileLineForModel(model: TmsNodeModel, project: Project): TmsNodeModel {
        model.file = null
        model.line = null

        val globalId = model.globalId ?: return model
        refreshFiles(project)

        for (file in projectFiles) {
            val lines = mutableListOf<String>()

            runReadAction {
                lines.addAll(FileDocumentManager.getInstance().getDocument(file)?.charsSequence?.lines().orEmpty())
            }

            val line: Int? = findTestByGlobalId(lines, globalId)
            if (line != null) {
                model.file = file
                model.line = line

                break
            }
        }

        return model
    }

    private fun refreshFiles(project: Project) {
        projectFiles.clear()

        runReadAction {
            projectFiles.addAll(
                FilenameIndex.getAllFilesByExt(
                    project, getExt(), GlobalSearchScope.projectScope(project)
                )
            )
        }
    }

    private fun findTestByGlobalId(lines: MutableList<String>, globalId: Long): Int? {
        val line: Int?
        for (counter in lines.indices) {
            if (lines[counter].contains(CodeSnippetUtils.getComparator()(globalId))) {
                line = counter
                return line
            }
        }
        return null
    }

    fun replaceMatches(
        root: DefaultMutableTreeNode,
        replaceAll: Boolean,
        results: Map<String, List<MatchInfo>>): MutableMap<String, FileInfo> {
        val allUpdateFiles = mutableMapOf<String, FileInfo>()

        root.children().toList().forEach { fileNode ->
            val updateFiles = replaceInFile(fileNode as DefaultMutableTreeNode, replaceAll, results)

            allUpdateFiles.putAll(updateFiles)
        }

        return allUpdateFiles
    }

    private fun getExt(): String {
        val framework: String? = TmsSettingsState.instance.getFramework()
        return when (framework) {
            FrameworkOption.BEHAVE.toString() -> ExtOption.GHERKIN.toString()
            FrameworkOption.NOSE.toString() -> ExtOption.PYTHON.toString()
            FrameworkOption.PYTEST.toString() -> ExtOption.PYTHON.toString()
            FrameworkOption.ROBOTFRAMEWORK.toString() -> ExtOption.ROBOT.toString()
            FrameworkOption.JUNIT.toString() -> ExtOption.JAVA.toString()
            FrameworkOption.MSTEST.toString() -> ExtOption.CSHARP.toString()
            FrameworkOption.NUNIT.toString() -> ExtOption.CSHARP.toString()
            FrameworkOption.XUNIT.toString() -> ExtOption.CSHARP.toString()
            FrameworkOption.SPECFLOW.toString() -> ExtOption.GHERKIN.toString()
            FrameworkOption.CODECEPTJS.toString() -> ExtOption.TYPESCRIPT.toString()
            FrameworkOption.CUCUMBER.toString() -> ExtOption.GHERKIN.toString()
            FrameworkOption.JEST.toString() -> ExtOption.TYPESCRIPT.toString()
            FrameworkOption.MOCHA.toString() -> ExtOption.TYPESCRIPT.toString()
            FrameworkOption.PLAYWRIGHT.toString() -> ExtOption.TYPESCRIPT.toString()
            FrameworkOption.TESTCAFE.toString() -> ExtOption.TYPESCRIPT.toString()
            else -> ExtOption.JAVA.toString()
        }
    }

    //TODO: refactoring
    private fun replaceInFile(
        fileNode: DefaultMutableTreeNode,
        replaceAll: Boolean,
        results: Map<String, List<MatchInfo>>
    ): MutableMap<String, FileInfo> {
        val updateFiles = mutableMapOf<String, FileInfo>()
        val filePath = fileNode.userObject.toString()
        val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$filePath")
            ?: return mutableMapOf()

        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
            ?: return mutableMapOf()

        val originalText = document.text
        val textBuilder = StringBuilder(originalText)
        val replacements = mutableListOf<Pair<IntRange, String>>()
        val matchesInfos = mutableListOf<MatchInfo>()
        val replacementInfos = mutableListOf<ReplacementInfo>()
        var offsetAdjustment = 0

        fileNode.children().toList().forEach { matchNode ->
            val checkBoxNode = (matchNode as DefaultMutableTreeNode).userObject as? CheckBoxNode ?: return@forEach
            val index = fileNode.getIndex(matchNode)

            if (!replaceAll && !checkBoxNode.isSelected) {
                return@forEach
            }

            results[filePath]?.get(index)?.let { matchInfo ->
                val start = matchInfo.start
                val end = matchInfo.end

                val adjustedStart = start + offsetAdjustment

                if (start >= 0 && end <= textBuilder.length) {
                    val originalMatch = textBuilder.substring(start, end)
                    val replacement = ParsingAnnotationsUtils.parse(originalMatch)
                    offsetAdjustment += replacement.length - (end - start)

                    matchesInfos.add(matchInfo)
                    replacementInfos.add(
                        ReplacementInfo(
                            text = replacement,
                            start = adjustedStart,
                            end = adjustedStart + replacement.length,
                            filePath = filePath,
                        )
                    )

                    replacements.add(start..end to replacement)
                }
            }
        }

        replacements.sortedByDescending { it.first.first }.forEach { (range, replacement) ->
            textBuilder.replace(range.first, range.last, replacement)
        }

        if (replacements.isNotEmpty()) {
            updateFiles[filePath] = FileInfo(
                filePath = filePath,
                oldText = originalText,
                newText = textBuilder.toString(),
                matches = matchesInfos,
                replacements = replacementInfos,
            )
        }

        return updateFiles
    }

    fun performReplacements(project: Project, replacements: Map<String, FileInfo>) {
        WriteCommandAction.runWriteCommandAction(project) {
            replacements.forEach { (filePath, fileInfo) ->
                VirtualFileManager.getInstance().findFileByUrl("file://$filePath")?.let { virtualFile ->
                    FileDocumentManager.getInstance().getDocument(virtualFile)?.let { document ->
                        document.setText(fileInfo.newText)
                        FileDocumentManager.getInstance().saveDocument(document)
                    }
                }
            }
        }
    }

    fun openFileAtMatch(project: Project, filePath: String, match: MatchInfo) {
        val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$filePath") ?: return

        if (match.lineNumber != -1) {
            val descriptor = OpenFileDescriptor(project, virtualFile, match.lineNumber, match.column)
            descriptor.navigate(true)
        } else {
            Messages.showInfoMessage(project, "Match not found in file.", "Info")
        }
    }
}