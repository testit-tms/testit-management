package ru.testit.management.windows.tools

import com.intellij.openapi.project.Project
import ru.testit.management.parsers.models.MatchInfo
import ru.testit.management.utils.VirtualFileUtils
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class SearchAllureMouseListener(project: Project, tree: JTree, results: MutableMap<String, List<MatchInfo>>) : MouseListener {
    private var _project = project
    private var _tree = tree
    private var _results = results

    override fun mouseClicked(event: MouseEvent) {
        val path = _tree.getPathForLocation(event.x, event.y) ?: return
        val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
        val userObject = node.userObject

        if (userObject is CheckBoxNode) {
            userObject.isSelected = !userObject.isSelected
            _tree.repaint()
        }

        if (event.clickCount == 2) {
            val index = node.parent.children().toList().indexOf(node)
            val filePath = (node.parent as DefaultMutableTreeNode).userObject.toString()
            val matchInfo = _results[filePath]?.get(index) ?: return

            VirtualFileUtils.openFileAtMatch(_project, filePath, matchInfo)
        }
    }

    override fun mousePressed(event: MouseEvent) {
    }

    override fun mouseReleased(event: MouseEvent) {
    }

    override fun mouseEntered(event: MouseEvent?) {
    }

    override fun mouseExited(event: MouseEvent?) {
    }
}
