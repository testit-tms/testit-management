package ru.testit.management.windows.tools

import com.intellij.icons.AllIcons
import java.awt.Component
import javax.swing.JCheckBox
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer

class CheckBoxTreeCellRenderer : TreeCellRenderer {
    private val checkBox = JCheckBox()
    private val defaultRenderer = javax.swing.tree.DefaultTreeCellRenderer()

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {
        val node = value as DefaultMutableTreeNode
        val userObject = node.userObject

        if (userObject is CheckBoxNode) {
            checkBox.text = userObject.text
            checkBox.isSelected = userObject.isSelected
            checkBox.isOpaque = false
            checkBox.icon = AllIcons.General.Information
            return checkBox
        }

        return defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
    }
}
