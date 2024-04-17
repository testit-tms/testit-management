package ru.testit.management.windows.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "ru.testit.settings.TmsSettingsState", storages = [Storage("TestItSettings.xml")])
@Service
class TmsSettingsState : PersistentStateComponent<TmsSettingsState> {
    var url: String = ""
    var projectId: String = ""
    var privateToken: String = ""

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
