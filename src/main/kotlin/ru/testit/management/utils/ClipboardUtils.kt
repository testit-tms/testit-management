package ru.testit.management.utils

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object ClipboardUtils {
    fun copyToClipboard(text: String) {
        Toolkit
            .getDefaultToolkit()
            .systemClipboard
            .setContents(StringSelection(text), null)
    }
}