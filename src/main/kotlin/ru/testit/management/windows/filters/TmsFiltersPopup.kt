package ru.testit.management.windows.filters

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
import javax.swing.JComponent

class TmsFiltersPopup private constructor(
    private val parent: JComponent,
    private val anchor: Point
) {
    fun show(): Any {
        var popupRef: Any? = null
        val window = TmsFiltersWindow(
            onApplyRequested = {
                (popupRef as? JBPopup)?.cancel()
            }
        )

        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(window.panel, parent)
            .setRequestFocus(true)
            .setCancelOnClickOutside(true)
            .setResizable(false)
            .createPopup()

        popupRef = popup
        popup.show(RelativePoint(parent, anchor))
        return popup
    }

    companion object {
        fun show(parent: JComponent, anchor: Point): Any {
            return TmsFiltersPopup(parent, anchor).show()
        }
    }
}
