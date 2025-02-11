package ru.testit.management.windows.tools

import ru.testit.client.model.WorkItemEntityTypes
import ru.testit.management.icons.TmsIcons
import java.awt.Component
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer

class TmsCellStyle : DefaultTreeCellRenderer() {
    init {
        setBorderSelectionColor(null)
        setBackgroundSelectionColor(null)
    }

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        sel: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

        val node = value as DefaultMutableTreeNode
        val model = node.userObject as TmsNodeModel
        val globalId = model.globalId
        val name = model.name.orEmpty()

        when {
            globalId == null && expanded -> {
                this.icon = openIcon
                this.text = name
            }

            globalId == null -> {
                this.icon = closedIcon
                this.text = name
            }

            else -> {
                setWorkItemIcon(model)
                this.text = "<html><i>$globalId</i> $name</html>"
            }
        }

        return this
    }

    private fun setWorkItemIcon(model: TmsNodeModel) {
        when (model.entityTypeName) {
            WorkItemEntityTypes.CHECK_LISTS -> {
                this.icon = getCheckListIcon(model.isAutomated)
            }

            WorkItemEntityTypes.SHARED_STEPS -> {
                this.icon = getSharedStepIcon(model.isAutomated)
            }

            else -> {
                this.icon = getTestCaseIcon(model.isAutomated)
            }
        }
    }

    private fun getCheckListIcon(isAutomated: Boolean): Icon {
        val checkListIcon = if (isAutomated) {
            TmsIcons.CheckListAutomated
        } else {
            TmsIcons.CheckList
        }

        return checkListIcon
    }

    private fun getSharedStepIcon(isAutomated: Boolean): Icon {
        val sharedStepIcon = if (isAutomated) {
            TmsIcons.SharedStepAutomated
        } else {
            TmsIcons.SharedStep
        }

        return sharedStepIcon
    }

    private fun getTestCaseIcon(isAutomated: Boolean): Icon {
        val testCaseIcon = if (isAutomated) {
            TmsIcons.TestCaseAutomated
        } else {
            TmsIcons.TestCase
        }

        return testCaseIcon
    }
}
