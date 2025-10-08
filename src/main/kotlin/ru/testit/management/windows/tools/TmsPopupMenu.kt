package ru.testit.management.windows.tools

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import ru.testit.management.clients.TmsClient
import ru.testit.management.utils.ClipboardUtils
import ru.testit.management.utils.CodeSnippetUtils
import ru.testit.management.utils.MessagesUtils
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

                ClipboardUtils.copyToClipboard(CodeSnippetUtils.getNewSnippet(node.userObject))
                showSimpleNotification(project);
            }
        }
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