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
import kotlinx.collections.immutable.persistentListOf
import org.jdesktop.swingx.JXTree
import ru.testit.client.model.SectionModel
import ru.testit.management.clients.TmsClient
import ru.testit.management.utils.MessagesUtils
import ru.testit.management.utils.VirtualFileUtils
import java.awt.Component
import javax.swing.SwingConstants
import javax.swing.UIManager
import javax.swing.tree.DefaultMutableTreeNode


class TmsToolWindow private constructor() : SimpleToolWindowPanel(true, true) {

    companion object {
        val instance: TmsToolWindow by lazy {
            TmsToolWindow()
        }
    }

    private var _tree: Component? = null

    init {
        showActionsToolbar()
    }

    fun refresh(project: Project) {
        if (_tree != null) {
            remove(_tree)
        }

        val label = getSpinner()
        add(label)
        showTree(label, project)
    }

    private fun showActionsToolbar() {
        val actionManager = ActionManager.getInstance()
        val group = DefaultActionGroup()

        val refreshProject = actionManager.getAction("ru.testit.management.SyncProjectAction")
        group.add(refreshProject)

        val openTmsSettings = actionManager.getAction("ru.testit.management.OpenSettingsAction")
        group.add(openTmsSettings)

        val actionToolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true)
        actionToolbar.targetComponent = this

        runInEdt { toolbar = actionToolbar.component }
    }

    private fun getSpinner(): Component {
        val label = JBLabel()

        label.verticalAlignment = SwingConstants.CENTER
        label.horizontalAlignment = SwingConstants.CENTER
        label.text = MessagesUtils.get("window.tool.spinner.text")

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

    private fun getTree(project: Project): Component {
        val tree = JXTree(getRootTreeNode(project))
        tree.cellRenderer = TmsCellStyle()
        tree.addMouseListener(TmsMouseListener(project, tree))

        return tree
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
                        sections.filter { s -> persistentListOf(parentSection.id, section.id).contains(s.id) }
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

    private fun getModelWithFileLineModified(model: TmsNodeModel, project: Project): TmsNodeModel {
        val globalId = model.globalId ?: return model
        VirtualFileUtils.refresh(project)

        for (file in VirtualFileUtils.projectJavaFiles) {
            val lines = mutableListOf<String>()

            runReadAction {
                lines.addAll(FileDocumentManager.getInstance().getDocument(file)?.charsSequence?.lines().orEmpty())
            }

            var line: Int? = null

            for (counter in lines.indices) {
                if (lines[counter].contains("@WorkItemIds(\"$globalId\")")) {
                    line = counter

                    break
                }
            }

            if (line != null) {
                model.file = file
                model.line = line

                break
            }
        }

        return model
    }
}