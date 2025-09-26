package ru.testit.management.windows.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import ru.testit.management.enums.FrameworkOption
import com.intellij.util.PlatformUtils


@State(name = "ru.testit.settings.TmsSettingsState", storages = [Storage("TestItSettings.xml")])
@Service
class TmsSettingsState : PersistentStateComponent<TmsSettingsState> {
    var url: String = ""
    var projectId: String = ""
    var privateToken: String = ""
    private var framework: String = getDefaultFramework()

    fun getFramework(): String {
        return framework
    }
    fun setFramework(value: String?) {
        if (value == null) framework = getDefaultFramework()
        else framework = value
    }

    private fun getDefaultFramework(): String {
        if (PlatformUtils.isPyCharm()) {
            return FrameworkOption.PYTEST.toString()
        } else if (PlatformUtils.isIntelliJ()) {
            return FrameworkOption.JUNIT.toString()
        } else if (PlatformUtils.isWebStorm()) {
            return FrameworkOption.PLAYWRIGHT.toString()
        } else if (PlatformUtils.isRider()) {
            return FrameworkOption.MSTEST.toString()
        }
        return FrameworkOption.JUNIT.toString()
    }

    override fun getState(): TmsSettingsState {
        return this
    }

    override fun loadState(state: TmsSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }


    companion object {
        val instance: TmsSettingsState
            get() = ApplicationManager.getApplication().getService(TmsSettingsState::class.java)
    }
}
