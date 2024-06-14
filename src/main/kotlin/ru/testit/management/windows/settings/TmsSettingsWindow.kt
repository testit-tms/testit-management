package ru.testit.management.windows.settings

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import ru.testit.management.clients.TmsClient
import ru.testit.management.utils.MessagesUtils
import java.net.URL
import java.util.*
import javax.swing.JLabel

class TmsSettingsWindow {
    @JvmField
    val panel: DialogPanel

    var projectId: Cell<JBTextField>? = null
    var privateToken: Cell<JBTextField>? = null
    var url: Cell<JBTextField>? = null
        get() = getValidUrl(field)

    private val _state = TmsSettingsState.instance
    private var _verifyLabel: Cell<JLabel>? = null

    init {
        panel = panel {
            row {
                browserLink(
                    MessagesUtils.get("window.settings.instruction.link.text"),
                    MessagesUtils.get("window.settings.instruction.link.url")
                )
            }
            groupRowsRange(MessagesUtils.get("window.settings.group.connection.name")) {
                row(MessagesUtils.get("window.settings.group.connection.url.name")) {
                    url = textField().bindText(_state::url).focused()
                }
                row(MessagesUtils.get("window.settings.group.connection.projectId.name")) {
                    projectId = textField().bindText(_state::projectId)
                }
                row(MessagesUtils.get("window.settings.group.connection.token.name")) {
                    privateToken = textField().bindText(_state::privateToken)
                }
                twoColumnsRow({
                    button(MessagesUtils.get("window.settings.group.connection.verify.button.text")) {
                        _verifyLabel?.component?.text = MessagesUtils.get(
                            "window.settings.group.connection.verify.label.start.text"
                        )

                        invokeLater {
                            verifySettings(
                                projectId?.component?.text,
                                privateToken?.component?.text,
                                url?.component?.text,
                                _verifyLabel?.component
                            )
                        }
                    }
                }) {
                    _verifyLabel = label(
                        MessagesUtils.get(
                            "window.settings.group.connection.verify.label.end.text"
                        )
                    )
                }
            }
        }
    }

    private fun getValidUrl(cell: Cell<JBTextField>?): Cell<JBTextField>? {
        val text = cell?.component?.text.orEmpty()

        if (text.endsWith('/')) {
            cell?.component?.text = text.dropLast(1)
        }

        return cell
    }

    private fun verifySettings(projectId: String?, privateToken: String?, url: String?, verifyLabel: JLabel?) {
        var errorMsg = getUrlValidationErrorMsg(url)

        if (errorMsg != null) {
            verifyLabel?.text = errorMsg

            return
        }

        errorMsg = getProjectIdValidationErrorMsg(projectId)

        if (errorMsg != null) {
            verifyLabel?.text = errorMsg

            return
        }

        if (privateToken.isNullOrBlank()) {
            verifyLabel?.text = MessagesUtils.get("window.settings.validation.token.error.text")

            return
        }

        errorMsg = TmsClient.getSettingsValidationErrorMsg(projectId.orEmpty(), privateToken, url.orEmpty())
        verifyLabel?.text = errorMsg ?: MessagesUtils.get("window.settings.validation.success.text")
    }

    private fun getUrlValidationErrorMsg(url: String?): String? {
        if (url.isNullOrBlank()) {
            return MessagesUtils.get("window.settings.validation.url.error.text")
        }

        try {
            URL(url)
        } catch (ignored: Throwable) {
            return MessagesUtils.get("window.settings.validation.url.error.text")
        }

        return null
    }

    private fun getProjectIdValidationErrorMsg(projectId: String?): String? {
        if (projectId.isNullOrBlank()) {
            return MessagesUtils.get("window.settings.validation.projectId.error.text")
        }

        try {
            UUID.fromString(projectId)
        } catch (ignored: Throwable) {
            return MessagesUtils.get("window.settings.validation.projectId.error.text")
        }

        return null
    }
}
