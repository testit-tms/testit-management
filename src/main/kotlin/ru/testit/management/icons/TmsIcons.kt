package ru.testit.management.icons

import com.intellij.openapi.util.IconLoader

object TmsIcons {
    @JvmField
    val CheckList = IconLoader.getIcon("/icons/check_list/checkList.svg", javaClass)

    @JvmField
    val CheckListAutomated = IconLoader.getIcon("/icons/check_list/checkListAutomated.svg", javaClass)

    @JvmField
    val SharedStep = IconLoader.getIcon("/icons/shared_step/sharedStep.svg", javaClass)

    @JvmField
    val SharedStepAutomated = IconLoader.getIcon("/icons/shared_step/sharedStepAutomated.svg", javaClass)

    @JvmField
    val TestCase = IconLoader.getIcon("/icons/test_case/testCase.svg", javaClass)

    @JvmField
    val TestCaseAutomated = IconLoader.getIcon("/icons/test_case/testCaseAutomated.svg", javaClass)
}