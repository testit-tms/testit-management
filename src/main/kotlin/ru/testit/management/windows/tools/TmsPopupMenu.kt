package ru.testit.management.windows.tools

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import ru.testit.management.clients.TmsClient
import ru.testit.management.utils.ClipboardUtils
import ru.testit.management.utils.CodeSnippetUtils
import ru.testit.management.utils.MessagesUtils
import ru.testit.management.utils.VirtualFileUtils
import ru.testit.management.windows.settings.TmsSettingsState
import javax.swing.JMenuItem
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import com.intellij.notification.*;


class TmsPopupMenu(tree: JTree, project: Project) : JBPopupMenu() {
    private val _state = TmsSettingsState.instance

    init {
        val copyItem = JBMenuItem(
            MessagesUtils.get("window.tool.popup.copy.text"),
            AllIcons.Actions.Copy
        )

        addCopyAction(copyItem, tree, project)
        add(copyItem)
    }

    // TODO: depends on property
    private fun addCopyAction(item: JMenuItem, tree: JTree, project: Project) {
        item.addActionListener {
            val component = tree.getLastSelectedPathComponent() ?: return@addActionListener
            val node = component as DefaultMutableTreeNode

            var model = node.userObject as TmsNodeModel
            if (node.isLeaf && model.id != null) {
                val fullModel = TmsClient(_state.url).getWorkItemById(model.id!!)

                model.preconditions = fullModel.preconditionSteps
                model.steps = fullModel.steps
                model.postconditions = fullModel.postconditionSteps

                var startTime = System.currentTimeMillis()
                model = getModelWithFileLineModified(model, project!!)
                var endTime = System.currentTimeMillis()
                println("Затраченное на индексацию строки время:  ${endTime - startTime} мс")

                node.userObject = model
                ClipboardUtils.copyToClipboard(CodeSnippetUtils.getNewSnippet(node.userObject))
                showSimpleNotification(project);
            }
        }
    }


    private fun getModelWithFileLineModified(model: TmsNodeModel, project: Project): TmsNodeModel {
        val globalId = model.globalId ?: return model
        VirtualFileUtils.refresh(project)

        // TODO: сделать зависимость .ext от выбранного в настройках фреймворка
        println("Всего файлов с нужным ext: " + VirtualFileUtils.projectJavaFiles.count())
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

    private fun showSimpleNotification(project: Project?) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("TestItPluginNotifications")
            .createNotification(
                "Copy",
                "Successful item copy",
                NotificationType.INFORMATION
            )
            .notify(project)
    }
}