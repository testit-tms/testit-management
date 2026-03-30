package ru.testit.management.windows.filters

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import ru.testit.management.utils.StringUtils


@State(name = "ru.testit.filters.TmsFilterState", storages = [Storage("TmsFilters.xml")])
@Service
class TmsFilterState : PersistentStateComponent<TmsFilterState> {
    var testCaseName: String? = null
    var testCaseGlobalId: Long? = null
    var isAutomation: String? = null

    fun setTestCaseGlobalId(text: String?) {
        testCaseGlobalId = StringUtils.textToLong(text)
    }

    override fun getState(): TmsFilterState = this

    override fun loadState(state: TmsFilterState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: TmsFilterState
            get() = ApplicationManager.getApplication().getService(TmsFilterState::class.java)
    }
}
