package ru.testit.management.windows.tools

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import org.jdesktop.swingx.JXTree
import ru.testit.client.model.SectionModel
import ru.testit.management.clients.TmsClient
import ru.testit.management.windows.differs.FileDiffWindow
import ru.testit.management.parsers.models.MatchInfo
import ru.testit.management.utils.CodeSnippetUtils
import ru.testit.management.utils.MessagesUtils
import ru.testit.management.utils.VirtualFileUtils
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode


class TmsToolWindow private constructor() : SimpleToolWindowPanel(true, true) {

    companion object {
        val instance: TmsToolWindow by lazy {
            TmsToolWindow()
        }
    }

    private var _tree: Component? = null
    private var _search: Component? = null

    init {
        showActionsToolbar()
    }

    fun refresh(project: Project) {
        if (_tree != null) {
            remove(_tree)
        }

        if (_search != null)
        {
            remove(_search)
        }

        val label = getSpinnerForRefresh()
        add(label)
        showTree(label, project)
    }

    fun research(project: Project, results: MutableMap<String, List<MatchInfo>>) {
        if (_tree != null) {
            remove(_tree)
        }

        if (_search != null)
        {
            remove(_search)
        }

        val label = getSpinnerForResearch()
        add(label)
        showSearchTree(label, project, results)
    }

    private fun showActionsToolbar() {
        val actionManager = ActionManager.getInstance()
        val group = DefaultActionGroup()

        val refreshProject = actionManager.getAction("ru.testit.management.SyncProjectAction")
        group.add(refreshProject)

        val openTmsSettings = actionManager.getAction("ru.testit.management.OpenSettingsAction")
        group.add(openTmsSettings)

        val searchAllure = actionManager.getAction("ru.testit.management.SearchAllureAction")
        group.add(searchAllure)

        val actionToolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true)
        actionToolbar.targetComponent = this

        runInEdt { toolbar = actionToolbar.component }
    }

    private fun getSpinnerForRefresh(): Component {
        val label = JBLabel()

        label.verticalAlignment = SwingConstants.CENTER
        label.horizontalAlignment = SwingConstants.CENTER
        label.text = MessagesUtils.get("window.tool.spinner.text")

        return label
    }

    private fun getSpinnerForResearch(): Component {
        val label = JBLabel()

        label.verticalAlignment = SwingConstants.CENTER
        label.horizontalAlignment = SwingConstants.CENTER
        label.text = MessagesUtils.get("window.tool.spinner.search.text")

        return label
    }

    private fun showTree(label: Component, project: Project) {
        val oldStyle = UIManager.get("Tree.rendererFillBackground")

        try {
            UIManager.put("Tree.rendererFillBackground", false)
            _tree = JBScrollPane(getTree(project))
            remove(label)
            add(_tree)
        } finally {
            UIManager.put("Tree.rendererFillBackground", oldStyle)
        }
    }

    private fun showSearchTree(label: Component, project: Project, results: MutableMap<String, List<MatchInfo>>) {
        val oldStyle = UIManager.get("Tree.rendererFillBackground")

        try {
            UIManager.put("Tree.rendererFillBackground", false)
            _search = getSearchTree(project, results)
            remove(label)
            add(_search)
        } finally {
            UIManager.put("Tree.rendererFillBackground", oldStyle)
        }
    }

    private fun getTree(project: Project): Component {
        val tree = JXTree(getRootTreeNode(project))
        tree.cellRenderer = TmsCellStyle()
        tree.addMouseListener(TmsMouseListener(project, tree))

        return tree
    }

    private fun getSearchTree(project: Project, results: MutableMap<String, List<MatchInfo>>): Component {
        val root = getSearchTreeNode(results)
        val tree = JXTree(root)
        tree.cellRenderer = CheckBoxTreeCellRenderer()

        tree.addMouseListener(SearchAllureMouseListener(project, tree, results))

        val buttonPanel = JPanel()
        val replaceSelectedButton = JButton("Replace selected")
        val replaceAllButton = JButton("Replace all")

        buttonPanel.add(replaceSelectedButton)
        buttonPanel.add(replaceAllButton)

        replaceSelectedButton.addActionListener {
            FileDiffWindow.instance.show(project, root, false, results)
        }

        replaceAllButton.addActionListener {
            FileDiffWindow.instance.show(project, root, true, results)
        }

        val scrollPane = JBScrollPane(tree)
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        mainPanel.add(buttonPanel, BorderLayout.SOUTH)

        return mainPanel
    }

    private fun getRootTreeNode(project: Project): DefaultMutableTreeNode? {
        TmsClient.refresh()

        val sections = TmsClient.getSections()
        val rootSection = sections.singleOrNull { s -> s.parentId == null }
        val rootTreeNode = getChildTreeNode(rootSection, project, sections)

        return rootTreeNode
    }

    private fun getChildTreeNode(
        parentSection: SectionModel?,
        project: Project,
        sections: Iterable<SectionModel>?
    ): DefaultMutableTreeNode? {
        if (parentSection == null) {
            return null
        }

        val parentSectionNode = DefaultMutableTreeNode(TmsNodeModel(parentSection.name, null))

        sections?.forEach { section ->
            if (section.parentId == parentSection.id) {
                parentSectionNode.add(
                    getChildTreeNode(
                        section,
                        project,
                        sections
                    )
                )
            }
        }

        TmsClient.getWorkItemsBySectionId(parentSection.id).forEach { workItem ->
            val model = TmsNodeModel(
                workItem.name,
                workItem.globalId,
                workItem.preconditionSteps,
                workItem.steps,
                workItem.postconditionSteps,
                workItem.entityTypeName,
                workItem.isAutomated,
            )

            parentSectionNode.add(DefaultMutableTreeNode(getModelWithFileLineModified(model, project)))
        }

        return parentSectionNode
    }

    private fun getSearchTreeNode(
        results: MutableMap<String, List<MatchInfo>>
    ): DefaultMutableTreeNode {
        val root = DefaultMutableTreeNode("Allure results")

        results.keys.forEach { filePath ->
            val fileNode = DefaultMutableTreeNode(filePath)
            val matches = results[filePath]!!.sortedBy { match -> match.start }
            results[filePath] = matches

            matches.forEach { matchInfo ->
                fileNode.add(
                    DefaultMutableTreeNode(
                        CheckBoxNode("${matchInfo.text} (line ${matchInfo.lineNumber + 1})")))
            }
            root.add(fileNode)
        }

        return root
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

    private fun getModelWithFileLineModified(model: TmsNodeModel, project: Project): TmsNodeModel {
        val globalId = model.globalId ?: return model
        VirtualFileUtils.refresh(project)

        for (file in VirtualFileUtils.projectJavaFiles) {
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
}