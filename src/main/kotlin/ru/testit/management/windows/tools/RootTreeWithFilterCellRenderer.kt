package ru.testit.management.windows.tools

import com.intellij.icons.AllIcons
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode
import java.awt.Component
import java.awt.Color
import java.awt.Graphics
import javax.swing.UIManager

class RootTreeWithFilterCellRenderer(
    private val isFiltersAvailable: () -> Boolean,
    private val isIconHovered: () -> Boolean
) : TmsCellStyle() {

    private val filterIcon = AllIcons.General.Filter

    override fun getTreeCellRendererComponent(
        tree: javax.swing.JTree,
        value: Any,
        sel: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {
        val component = super.getTreeCellRendererComponent(
            tree,
            value,
            sel,
            expanded,
            leaf,
            row,
            hasFocus
        )

        val node = value as? DefaultMutableTreeNode ?: return component
        val isRootNode = node.parent == null

        if (!isRootNode || !isFiltersAvailable()) {
            return component
        }

        val baseIcon = (this.icon as? Icon) ?: return component
        val hovered = isIconHovered()

        val componentBg = UIManager.getColor("Component.background")
        val normalBg = UIManager.getColor("Button.background") ?: componentBg
        val hoverBg = UIManager.getColor("Button.hoverBackground")
            ?: UIManager.getColor("Component.hoverBackground")
            ?: normalBg

        val hoverFill = hoverBg?.let { it.withAlpha(90) }

        val composedIcon = FilterSquareCompositeIcon(
            leftIcon = filterIcon,
            rightIcon = baseIcon,
            normalBackground = null,
            hoverBackground = hoverFill,
            gap = UIManager.getInt("Tree.iconTextGap").takeIf { it > 0 } ?: 4
            ,
            isHovered = hovered
        )
        this.icon = composedIcon
        return component
    }

    private fun Color.withAlpha(alpha: Int): Color {
        val clamped = alpha.coerceIn(0, 255)
        return Color(red, green, blue, clamped)
    }

    private class FilterSquareCompositeIcon(
        private val leftIcon: Icon,
        private val rightIcon: Icon,
        private val normalBackground: Color?,
        private val hoverBackground: Color?,
        private val gap: Int,
        private val isHovered: Boolean
    ) : Icon {
        private val squareSize: Int = maxOf(leftIcon.iconWidth, leftIcon.iconHeight)
        private val cornerRadius: Int = 4

        override fun getIconWidth(): Int = squareSize + gap + rightIcon.iconWidth

        override fun getIconHeight(): Int = maxOf(squareSize, rightIcon.iconHeight)

        override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
            val h = iconHeight

            val squareY = y + (h - squareSize) / 2

            if (isHovered) {
                hoverBackground?.let { bg ->
                    g.color = bg
                    g.fillRoundRect(
                        x,
                        squareY,
                        squareSize,
                        squareSize,
                        cornerRadius,
                        cornerRadius)
                }
            }

            val leftX = x + (squareSize - leftIcon.iconWidth) / 2
            val leftY = squareY + (squareSize - leftIcon.iconHeight) / 2
            leftIcon.paintIcon(c, g, leftX, leftY)

            val rightX = x + squareSize + gap
            val rightY = y + (h - rightIcon.iconHeight) / 2
            rightIcon.paintIcon(c, g, rightX, rightY)
        }
    }
}
