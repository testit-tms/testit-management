package ru.testit.management.windows.differs

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import ru.testit.management.parsers.models.FileInfo
import ru.testit.management.parsers.models.MatchInfo
import ru.testit.management.utils.SearchAllureUtils
import ru.testit.management.utils.VirtualFileUtils
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class FileDiffWindow private constructor() {
    companion object {
        val instance: FileDiffWindow by lazy {
            FileDiffWindow()
        }
    }

    private val addedAttributes = TextAttributes().apply {
        backgroundColor = JBColor.GREEN
        fontType = Font.BOLD
    }

    private val deletedAttributes = TextAttributes().apply {
        backgroundColor = JBColor.RED
        fontType = Font.BOLD
        effectType = EffectType.STRIKEOUT
        effectColor = JBColor.BLACK
    }

    fun show(
        project: Project,
        root: DefaultMutableTreeNode,
        replaceAll: Boolean,
        results: Map<String, List<MatchInfo>>
    ) {
        val replacementFiles = VirtualFileUtils.replaceMatches(root, replaceAll, results)

        if (replacementFiles.isEmpty()) {
            Messages.showInfoMessage(project, "No changes to preview.", "Info")
            return
        }

        val fileListModel = DefaultListModel<String>()
        replacementFiles.keys.forEach { filePath -> fileListModel.addElement(filePath) }

        val fileList = JList(fileListModel)
        fileList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        fileList.selectedIndex = 0

        val editorFactory = EditorFactory.getInstance()
        val editorPanel = JPanel(BorderLayout())

        var originalEditor: EditorEx? = null
        var modifiedEditor: EditorEx? = null

        fun updateEditors(selectedFile: FileInfo) {
            originalEditor?.let { editorFactory.releaseEditor(it) }
            modifiedEditor?.let { editorFactory.releaseEditor(it) }

            val originalDoc = editorFactory.createDocument(selectedFile.oldText)
            val modifiedDoc = editorFactory.createDocument(selectedFile.newText)

            originalEditor = editorFactory.createEditor(originalDoc, project) as EditorEx
            modifiedEditor = editorFactory.createEditor(modifiedDoc, project) as EditorEx

            listOf(originalEditor, modifiedEditor).forEach { editor ->
                (editor)?.apply {
                    isViewer = true
                    setVerticalScrollbarVisible(true)
                    settings.apply {
                        isLineNumbersShown = true
                        isFoldingOutlineShown = false
                        isLineMarkerAreaShown = true
                        isIndentGuidesShown = true
                    }
                }
            }

            highlightChanges(originalEditor!!, modifiedEditor!!, selectedFile)

            editorPanel.removeAll()
            val splitter = JBSplitter(false).apply {
                firstComponent = originalEditor?.component
                secondComponent = modifiedEditor?.component
                proportion = 0.5f
            }
            editorPanel.add(splitter, BorderLayout.CENTER)
            editorPanel.revalidate()
            editorPanel.repaint()
        }

        fileList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val selectedFile = fileList.selectedValue ?: return@addListSelectionListener
                replacementFiles[selectedFile]?.let { updateEditors(it) }
            }
        }

        if (fileListModel.size() > 0) {
            replacementFiles[fileListModel.getElementAt(0)]?.let { updateEditors(it) }
        }

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT).apply {
            leftComponent = JScrollPane(fileList)
            rightComponent = editorPanel
            dividerLocation = 200
        }

        val confirmButton = JButton("Replace")

        val dialog = object : DialogWrapper(project) {
            init {
                init()
                title = "Preview Changes"
            }

            override fun createCenterPanel(): JComponent {
                val mainPanel = JPanel(BorderLayout())
                mainPanel.add(splitPane, BorderLayout.CENTER)

                val buttonPanel = JPanel()
                buttonPanel.add(confirmButton)
                mainPanel.add(buttonPanel, BorderLayout.SOUTH)

                return mainPanel
            }

            override fun createActions(): Array<Action> {
                return emptyArray()
            }

            override fun getPreferredSize(): Dimension = Dimension(800, 600)

            override fun dispose() {
                originalEditor?.let { editorFactory.releaseEditor(it) }
                modifiedEditor?.let { editorFactory.releaseEditor(it) }
                super.dispose()
            }
        }

        confirmButton.addActionListener {
            VirtualFileUtils.performReplacements(project, replacementFiles)
            SearchAllureUtils.research()
            dialog.close(DialogWrapper.OK_EXIT_CODE)
        }

        dialog.show()
    }

    private fun highlightChanges(originalEditor: EditorEx, modifiedEditor: EditorEx, selectedFile: FileInfo) {
        val originalMarkers = originalEditor.markupModel
        val modifiedMarkers = modifiedEditor.markupModel

        originalMarkers.removeAllHighlighters()
        modifiedMarkers.removeAllHighlighters()

        selectedFile.matches.forEach { match ->
            originalMarkers.addRangeHighlighter(
                match.start,
                match.end,
                HighlighterLayer.SELECTION - 1,
                deletedAttributes,
                HighlighterTargetArea.EXACT_RANGE
            )
        }

        selectedFile.replacements.forEach { replacement ->
            modifiedMarkers.addRangeHighlighter(
                replacement.start,
                replacement.end,
                HighlighterLayer.SELECTION - 1,
                addedAttributes,
                HighlighterTargetArea.EXACT_RANGE
            )
        }

        originalEditor.scrollingModel.addVisibleAreaListener {
            val visibleArea = originalEditor.scrollingModel.visibleArea
            val centerY = visibleArea.y + visibleArea.height / 2
            val lineNumber = originalEditor.yToVisualLine(centerY)
            val column = originalEditor.caretModel.visualPosition.column

            modifiedEditor.scrollingModel.scrollTo(
                LogicalPosition(lineNumber, column),
                ScrollType.CENTER
            )
        }
    }
}