package ru.testit.management.windows.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import ru.testit.management.utils.MessagesUtils
import ru.testit.management.utils.SyncUtils
import javax.swing.JComponent

class TmsSettingsFactory : Configurable {
    private val _state = TmsSettingsState.instance
    private var _window: TmsSettingsWindow? = null

    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return MessagesUtils.get("window.settings.name")
    }

    override fun createComponent(): JComponent? {
        _window = TmsSettingsWindow()

        return _window?.panel
    }

    override fun isModified(): Boolean {
        var modified = _window?.url?.component?.text != _state.url
        modified = modified or (_window?.projectId?.component?.text != _state.projectId)
        modified = modified or (_window?.privateToken?.component?.text != _state.privateToken)
        modified = modified or (_window?.frameworkComboBox?.component?.item != _state.getFramework())
        return modified
    }

    override fun apply() {
        _state.url = _window?.url?.component?.text.orEmpty()
        _state.projectId = _window?.projectId?.component?.text.orEmpty()
        _state.privateToken = _window?.privateToken?.component?.text.orEmpty()
        _state.setFramework(_window?.frameworkComboBox?.component?.item.orEmpty())
        SyncUtils.refresh()
    }

    override fun reset() {
        _window?.url?.component?.text = _state.url
        _window?.projectId?.component?.text = _state.projectId
        _window?.privateToken?.component?.text = _state.privateToken
        _window?.frameworkComboBox?.component?.item = _state.getFramework()
    }

    override fun disposeUIResources() {
        _window = null
    }
}
