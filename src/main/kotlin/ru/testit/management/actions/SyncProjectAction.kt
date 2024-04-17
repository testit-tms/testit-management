package ru.testit.management.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.testit.management.utils.SyncUtils

class SyncProjectAction : AnAction() {
    override fun actionPerformed(ignored: AnActionEvent) {
        SyncUtils.refresh()
    }
}
