package ru.testit.management.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.testit.management.utils.SearchAllureUtils


class SearchAllureAction : AnAction() {
    override fun actionPerformed(ignored: AnActionEvent) {
        SearchAllureUtils.research()
    }
}