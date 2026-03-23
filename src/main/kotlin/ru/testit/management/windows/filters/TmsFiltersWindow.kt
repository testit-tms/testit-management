package ru.testit.management.windows.filters

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import ru.testit.management.utils.StringUtils
import ru.testit.management.utils.SyncUtils

class TmsFiltersWindow(
    private val onApplyRequested: (() -> Unit)? = null
) {
    @JvmField
    val panel: DialogPanel

    var testCaseName: Cell<JBTextField>? = null
    var testCaseGlobalId: Cell<JBTextField>? = null
    var isAutomation: Cell<ComboBox<String>>? = null

    private val _state = TmsFilterState.instance

    init {
        panel = panel {
            row("Name") {
                testCaseName = textField()
            }
            row("GlobalId") {
                testCaseGlobalId = textField()
            }
            row("IsAutomation") {
                isAutomation = comboBox(listOf("Any", "True", "False"))
            }

            row {
                button("Apply") {
                    applyToState()
                    SyncUtils.refresh()
                    onApplyRequested?.invoke()
                }
            }
        }

        resetFromState()
    }

    fun resetFromState() {
        testCaseName?.component?.text = _state.testCaseName
        testCaseGlobalId?.component?.text = _state.testCaseGlobalId?.toString()
        isAutomation?.component?.selectedItem = _state.isAutomation
    }

    fun applyToState() {
        _state.testCaseName = testCaseName?.component?.text.orEmpty()
        _state.testCaseGlobalId = StringUtils.textToLong(testCaseGlobalId?.component?.text.orEmpty())
        _state.isAutomation = isAutomation?.component?.selectedItem as? String
    }
}
