package ru.testit.management.windows.tools

import com.intellij.openapi.vfs.VirtualFile
import ru.testit.client.model.StepModel
import ru.testit.client.model.WorkItemEntityTypes

class TmsNodeModel(
    var name: String?,
    var globalId: Long? = null,
    var preconditions: Iterable<StepModel>? = null,
    var steps: Iterable<StepModel>? = null,
    var postconditions: Iterable<StepModel>? = null,
    var entityTypeName: WorkItemEntityTypes? = null,
    var isAutomated: Boolean = false,
    var file: VirtualFile? = null,
    var line: Int? = null
)