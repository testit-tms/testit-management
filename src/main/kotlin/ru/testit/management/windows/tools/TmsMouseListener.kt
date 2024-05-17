package ru.testit.management.windows.tools

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class TmsMouseListener(project: Project, tree: JTree) : MouseListener {
    private var _project = project
    private var _tree = tree

    override fun mouseClicked(event: MouseEvent?) {
    }

    override fun mousePressed(event: MouseEvent) {
        val row = _tree.getClosestRowForLocation(event.x, event.y)
        _tree.setSelectionRow(row)

        if (event.isPopupTrigger) {
            showPopup(event)
        } else if (event.clickCount == 2) {
            tryOpenTest()
        }
    }

    override fun mouseReleased(event: MouseEvent) {
        if (event.isPopupTrigger) {
            showPopup(event)
        }
    }

    override fun mouseEntered(event: MouseEvent?) {
    }

    override fun mouseExited(event: MouseEvent?) {
    }

    private fun tryOpenTest() {
        val node = _tree.getLastSelectedPathComponent() as DefaultMutableTreeNode
        val model = node.userObject as TmsNodeModel

        var descriptor: OpenFileDescriptor? = null

        if (model.file != null && model.line != null) {
            descriptor = OpenFileDescriptor(
                _project,
                model.file as VirtualFile,
                model.line as Int,
                0,
                true
            )
        }

        descriptor?.let {
            FileEditorManager.getInstance(_project).openTextEditor(it, true)
        }

        descriptor?.dispose()
    }

    private fun showPopup(event: MouseEvent) {
        TmsPopupMenu(_tree).show(event.component, event.x, event.y)
    }
}
