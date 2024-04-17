package ru.testit.management.utils

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

object VirtualFileUtils {
    val projectJavaFiles = mutableSetOf<VirtualFile>()

    fun refresh(project: Project) {
        projectJavaFiles.clear()

        runReadAction {
            projectJavaFiles.addAll(
                FilenameIndex.getAllFilesByExt(
                    project, "java", GlobalSearchScope.projectScope(project)
                )
            )
        }
    }
}