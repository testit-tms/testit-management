package ru.testit.management.utils

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import ru.testit.management.windows.tools.TmsToolWindow
import java.util.concurrent.TimeUnit
import java.util.logging.Logger


object SyncUtils {
    private var _isRefreshing: Boolean = false
    private val _logger = Logger.getLogger(SyncUtils::class.java.simpleName)

    fun refresh() {
        synchronized(TmsToolWindow.instance) {
            if (_isRefreshing) {
                return
            }

            _isRefreshing = true
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val project = DataManager
                    .getInstance()
                    .dataContextFromFocusAsync
                    .then { PlatformDataKeys.PROJECT.getData(it) }
                    .blockingGet(3, TimeUnit.SECONDS)

                if (project != null) {
                    TmsToolWindow.instance.refresh(project)
                }
            } catch (exception: Throwable) {
                _logger.severe { exception.message }
            } finally {
                _isRefreshing = false
            }
        }
    }
}