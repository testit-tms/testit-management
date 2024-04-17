package ru.testit.management.windows.tools

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import ru.testit.management.utils.SyncUtils
import javax.swing.JComponent


class TmsToolFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        addContent(TmsToolWindow.instance, toolWindow)
        SyncUtils.refresh()
    }

    private fun addContent(component: JComponent, toolWindow: ToolWindow) {
        val content = ContentFactory.getInstance().createContent(component, null, false)
        content.isCloseable = false
        toolWindow.contentManager.addContent(content)
    }
}
