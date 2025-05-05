package ru.testit.management.windows.tools

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import ru.testit.management.utils.ClipboardUtils
import ru.testit.management.utils.CodeSnippetUtils
import ru.testit.management.utils.MessagesUtils
import javax.swing.JMenuItem
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class TmsPopupMenu(tree: JTree) : JBPopupMenu() {
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

            if (node.isLeaf) {
                ClipboardUtils.copyToClipboard(CodeSnippetUtils.getNewSnippet(node.userObject))
            }
        }
    }
}