package ru.testit.management.windows.tools

import com.intellij.icons.AllIcons
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

class TmsPopupMenu(tree: JTree) : JBPopupMenu() {
    private val _state = TmsSettingsState.instance

    init {
        val copyItem = JBMenuItem(
            MessagesUtils.get("window.tool.popup.copy.text"),
            AllIcons.Actions.Copy
        )

        addCopyAction(copyItem, tree)
        add(copyItem)
    }

    // TODO: depends on property
    private fun addCopyAction(item: JMenuItem, tree: JTree) {
        item.addActionListener {
            val component = tree.getLastSelectedPathComponent() ?: return@addActionListener
            val node = component as DefaultMutableTreeNode

            val model = node.userObject as TmsNodeModel
            if (node.isLeaf && model.id != null) {
                val fullModel = TmsClient(_state.url).getWorkItemById(model.id!!)

                model.preconditions = fullModel.preconditionSteps
                model.steps = fullModel.steps
                model.postconditions = fullModel.postconditionSteps

                node.userObject = model
                ClipboardUtils.copyToClipboard(CodeSnippetUtils.getNewSnippet(node.userObject))
            }
        }
    }
}