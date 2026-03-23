package ru.testit.management.windows.tools

import com.intellij.icons.AllIcons
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import java.awt.Rectangle
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import ru.testit.management.windows.filters.TmsFiltersPopup

class RootWithFilterIconMouseListener(
    private val tree: JTree,
    private val isFiltersAvailable: () -> Boolean
) : MouseAdapter() {

    private val filterIcon = AllIcons.General.Filter
    private val filterSize: Int = maxOf(filterIcon.iconWidth, filterIcon.iconHeight)

    var isIconHovered: Boolean = false
        private set

    override fun mouseMoved(e: MouseEvent) {
        if (!isFiltersAvailable()) {
            setHovered(false)
            return
        }

        val path = tree.getPathForLocation(e.x, e.y) ?: run {
            setHovered(false)
            return
        }
        val node = path.lastPathComponent as? DefaultMutableTreeNode ?: run {
            setHovered(false)
            return
        }

        if (node.parent != null) {
            setHovered(false)
            return
        }

        val rowBounds = tree.getPathBounds(path) ?: run {
            setHovered(false)
            return
        }

        val filterRect = Rectangle(
            rowBounds.x,
            rowBounds.y + (rowBounds.height - filterSize) / 2,
            filterSize,
            filterSize
        )

        setHovered(filterRect.contains(e.point))
    }

    override fun mouseExited(e: MouseEvent) {
        setHovered(false)
    }

    override fun mousePressed(e: MouseEvent) {
        if (e.isConsumed) return
        if (!isFiltersAvailable()) return

        val path = tree.getPathForLocation(e.x, e.y) ?: return
        val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
        if (node.parent != null) return

        val rowBounds = tree.getPathBounds(path) ?: return
        val gap = javax.swing.UIManager.getInt("Tree.iconTextGap").takeIf { it > 0 } ?: 4

        val filterRect = Rectangle(
            rowBounds.x,
            rowBounds.y + (rowBounds.height - filterSize) / 2,
            filterSize + gap,
            filterSize
        )

        if (!filterRect.contains(e.point)) return

        val anchor = Point(filterRect.x, filterRect.y + filterRect.height)
        TmsFiltersPopup.show(tree, anchor)
        e.consume()
    }

    private fun setHovered(value: Boolean) {
        if (isIconHovered == value) return
        isIconHovered = value
        tree.repaint()
    }
}
