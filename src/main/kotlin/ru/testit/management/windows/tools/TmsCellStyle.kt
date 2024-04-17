package ru.testit.management.windows.tools

import ru.testit.client.model.WorkItemEntityTypes
import ru.testit.management.icons.TmsIcons
import java.awt.Component
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
                this.text = name
            }

            globalId == null && !expanded -> {
                this.icon = closedIcon
                this.text = name
            }

            else -> {
                when (model.entityTypeName) {
                    WorkItemEntityTypes.CHECKLISTS -> {
                        if (model.isAutomated) {
                            this.icon = TmsIcons.CheckListAutomated
                        } else {
                            this.icon = TmsIcons.CheckList
                        }
                    }

                    WorkItemEntityTypes.SHAREDSTEPS -> {
                        if (model.isAutomated) {
                            this.icon = TmsIcons.SharedStepAutomated
                        } else {
                            this.icon = TmsIcons.SharedStep
                        }
                    }

                    else -> {
                        if (model.isAutomated) {
                            this.icon = TmsIcons.TestCaseAutomated
                        } else {
                            this.icon = TmsIcons.TestCase
                        }
                    }
                }

                this.text = "<html><i>$globalId</i> $name</html>"
            }
        }

        return this
    }
}
