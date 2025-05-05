package ru.testit.management.utils

import ru.testit.management.enums.FrameworkOption
import ru.testit.management.parsers.PytestParser
import ru.testit.management.parsers.models.MatchInfo
import ru.testit.management.windows.settings.TmsSettingsState

object ParsingAnnotationsUtils {
    fun getAllPatterns(): List<Regex> {
        val framework: String? = TmsSettingsState.instance.getFramework()
        val patterns = when (framework) {
            FrameworkOption.PYTEST.toString() -> PytestParser.getPatterns()
//            FrameworkOption.RobotFramework.toString() -> RobotFrameworkParser.getAllPatterns()
            else -> PytestParser.getPatterns()
        }
        return patterns
    }

    fun parse(allureCode: String, matchInfo: MatchInfo): String {
        val framework: String? = TmsSettingsState.instance.getFramework()
        val tmsCode = when (framework) {
            FrameworkOption.PYTEST.toString() -> PytestParser.parse(allureCode, matchInfo)
//            FrameworkOption.RobotFramework.toString() -> RobotFrameworkParser.parse(allureCode, matchInfo)
            else -> PytestParser.parse(allureCode, matchInfo)
        }
        return tmsCode
    }
}