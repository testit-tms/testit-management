package ru.testit.management.parsers.regexps

object PlaywrightRegexp {
    const val ANNOTATION_SEPARATOR = "\\."
    const val SPACES_AND_LINE_BREAKS = "[\\s\\n]*"
    const val EVERYTHING_IN_PARENTHESES = "\\([\\s\\S][^)]{1,}\\)"
    const val EVERYTHING_IN_BRACES = "\\{[\\s\\S][^}]{1,}\\}"
    const val ALLURE_OBJECT = "allure"
    const val ALLURE_METHOD = ALLURE_OBJECT + ANNOTATION_SEPARATOR
    const val IMPORT_ALLURE_OBJECT = "import" + SPACES_AND_LINE_BREAKS + "\\*" + SPACES_AND_LINE_BREAKS +
            "as" + SPACES_AND_LINE_BREAKS + ALLURE_OBJECT + SPACES_AND_LINE_BREAKS + "from" + SPACES_AND_LINE_BREAKS +
            "\"allure-js-commons\""
    const val ALLURE_DYNAMIC_DISPLAY_NAME = ALLURE_METHOD + "displayName"
    const val ALLURE_DYNAMIC_DESCRIPTION = ALLURE_METHOD + "description"
    const val ALLURE_DYNAMIC_DESCRIPTION_HTML = ALLURE_METHOD + "descriptionHtml"
    const val ALLURE_DYNAMIC_LINK = ALLURE_METHOD + "link" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_LINKS = ALLURE_METHOD + "links" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_ISSUE = ALLURE_METHOD + "issue" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_TMS = ALLURE_METHOD + "tms" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_TAG = ALLURE_METHOD + "tag" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_TAGS = ALLURE_METHOD + "tags" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_LABEL = ALLURE_METHOD + "label" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_LABELS = ALLURE_METHOD + "labels" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_ALLURE_ID = ALLURE_METHOD + "allureId" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_TEST_CASE_ID = ALLURE_METHOD + "testCaseId" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_HISTORY_ID = ALLURE_METHOD + "historyId" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_OWNER = ALLURE_METHOD + "owner" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_SEVERITY = ALLURE_METHOD + "severity" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_LAYER = ALLURE_METHOD + "layer" + EVERYTHING_IN_PARENTHESES
    const val ALLURE_DYNAMIC_EPIC = ALLURE_METHOD + "epic"
    const val ALLURE_DYNAMIC_FEATURE = ALLURE_METHOD + "feature"
    const val ALLURE_DYNAMIC_STORY = ALLURE_METHOD + "story"
    const val ALLURE_DYNAMIC_PARENT_SUITE = ALLURE_METHOD + "parentSuite"
    const val ALLURE_DYNAMIC_SUITE = ALLURE_METHOD + "suite"
    const val ALLURE_DYNAMIC_SUB_SUITE = ALLURE_METHOD + "subSuite"
    const val ALLURE_DYNAMIC_PARAMETER = ALLURE_METHOD + "parameter"
    const val ALLURE_DYNAMIC_ATTACHMENT_WRITE = ALLURE_METHOD + "attachment"
    const val ALLURE_DYNAMIC_STEP = ALLURE_METHOD + "step"
    const val VARIABLE = "[^'\",\\s)]+"
    const val VALUE = "'[^']*'|\"[^\"]*\""
    const val ASSIGNMENT = "\\s*:\\s*"

    const val LINK_URL_PARAMETER_NAME = "url"
    const val LINK_NAME_PARAMETER_NAME = "name"
    const val LINK_URL_PARAMETER = "(?:\\(\\s*" +
            "(?<$LINK_URL_PARAMETER_NAME>$VARIABLE|$VALUE)"
    const val LINK_NAME_PARAMETER = "(?:" +
            "\\(\\s*(?:$VARIABLE|$VALUE)\\s*,\\s*" +
            "(?<$LINK_NAME_PARAMETER_NAME>$VARIABLE|$VALUE)"

    const val LINK_IN_LINKS_URL_PARAMETER = "\\{\\s*" +
            "$LINK_URL_PARAMETER_NAME$ASSIGNMENT" +
            "(?<$LINK_URL_PARAMETER_NAME>$VARIABLE|$VALUE)"
    const val LINK_IN_LINKS_NAME_PARAMETER = "\\{\\s*$VARIABLE|$VALUE\\s*,\\s*" +
            "$LINK_NAME_PARAMETER_NAME$ASSIGNMENT" +
            "(?<$LINK_NAME_PARAMETER_NAME>$VARIABLE|$VALUE)"

    const val LABEL_NAME_PARAMETER_NAME = "name"
    const val LABEL_VALUE_PARAMETER_NAME = "value"
    const val LABEL_NAME_PARAMETER = "(?:\\(\\s*" +
            "(?<$LABEL_NAME_PARAMETER_NAME>${PytestRegexp.VARIABLE}|${PytestRegexp.VALUE})"
    const val LABEL_VALUE_PARAMETER = "(?:" +
            "\\(\\s*(?:${PytestRegexp.VARIABLE}|${PytestRegexp.VALUE})\\s*,\\s*" +
            "(?<$LABEL_VALUE_PARAMETER_NAME>${PytestRegexp.VARIABLE}|${PytestRegexp.VALUE})"

    const val ANNOTATION_SEPARATOR_OBJECT = "."
    const val PARAMETERS_SEPARATOR_OBJECT = ", "
    const val TMS_OBJECT = "testit"
    const val IMPORT_TMS_OBJECT = "import { $TMS_OBJECT } from \"testit-adapter-playwright\""
    const val TMS_METHOD_OBJECT = TMS_OBJECT + ANNOTATION_SEPARATOR_OBJECT
    const val TMS_DISPLAY_NAME = TMS_METHOD_OBJECT + "displayName"
    const val TMS_DESCRIPTION = TMS_METHOD_OBJECT + "description"
    const val TMS_LABELS = TMS_METHOD_OBJECT + "labels"
    const val TMS_STEP = TMS_METHOD_OBJECT + "step"
    const val TMS_LINKS = TMS_METHOD_OBJECT + "links"
    const val TMS_NAMESPACE = TMS_METHOD_OBJECT + "namespace"
    const val TMS_CLASSNAME = TMS_METHOD_OBJECT + "classname"
    const val TMS_ADD_PARAMETER = TMS_METHOD_OBJECT + "params"
    const val TMS_ADD_ATTACHMENTS = TMS_METHOD_OBJECT + "addAttachments"
}