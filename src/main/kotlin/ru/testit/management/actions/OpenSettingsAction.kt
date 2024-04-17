package ru.testit.management.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import ru.testit.management.utils.MessagesUtils

class OpenSettingsAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(
            event.project,
            MessagesUtils.get("action.settings.name")
        )
    }
}
