package ru.testit.management.parsers

import ru.testit.management.parsers.models.MatchInfo

// TODO: move all regex patterns to the separate file
// TODO: move all objects to the separate file
object JUnitParser {
    private const val ANNOTATION_SEPARATOR = "\\."
    private const val EVERYTHING_IN_PARENTHESES = "\\([\\s\\S][^)]{1,}\\)"
    private const val ALLURE_OBJECT = "Allure"
    private const val ANNOTATION = "@"
    private const val IMPORT_ALLURE = "import io" + ANNOTATION_SEPARATOR + "qameta" + ANNOTATION_SEPARATOR + "allure"
    private const val IMPORT_ALLURE_OBJECT = IMPORT_ALLURE + ANNOTATION_SEPARATOR + ALLURE_OBJECT
    private const val IMPORT_JUPITER = "import org" + ANNOTATION_SEPARATOR + "junit" +
            ANNOTATION_SEPARATOR + "jupiter" + ANNOTATION_SEPARATOR + "api" + ANNOTATION_SEPARATOR
    private const val IMPORT_JUPITER_DISPLAY_NAME = IMPORT_JUPITER + "DisplayName"
    private const val IMPORT_ALLURE_DESCRIPTION = IMPORT_ALLURE + ANNOTATION_SEPARATOR + "Description"
    private const val IMPORT_ALLURE_STEP = IMPORT_ALLURE + ANNOTATION_SEPARATOR + "Step"
    private const val ALLURE_LINK = ANNOTATION + "Link" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_ISSUE = ANNOTATION + "Issue" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_TMS_LINK = ANNOTATION + "TmsLink" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_OWNER = ANNOTATION + "Owner"
    private const val ALLURE_ID = ANNOTATION + "AllureId"
    private const val ALLURE_EPIC = ANNOTATION + "Epic"
    private const val ALLURE_FEATURE = ANNOTATION + "Feature"
    private const val ALLURE_STORY = ANNOTATION + "Story"
    private const val JUPITER_TEST_WITH_PARAMETERS = ANNOTATION + "Test" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_STEP_WITH_PARAMETERS = ANNOTATION + "Step" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_METHOD = ALLURE_OBJECT + ANNOTATION_SEPARATOR
    private const val ALLURE_DYNAMIC_DESCRIPTION = ALLURE_METHOD + "description"
    private const val ALLURE_DYNAMIC_DESCRIPTION_HTML = ALLURE_OBJECT + "descriptionHtml"
    private const val ALLURE_DYNAMIC_LINK = ALLURE_METHOD + "link" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_DYNAMIC_ISSUE = ALLURE_METHOD + "issue" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_DYNAMIC_TMS = ALLURE_METHOD + "tms" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_DYNAMIC_LABEL = ALLURE_METHOD + "label" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_DYNAMIC_EPIC = ALLURE_METHOD + "epic"
    private const val ALLURE_DYNAMIC_FEATURE = ALLURE_METHOD + "feature"
    private const val ALLURE_DYNAMIC_STORY = ALLURE_METHOD + "story"
    private const val ALLURE_DYNAMIC_SUITE = ALLURE_METHOD + "suite"
    private const val ALLURE_DYNAMIC_PARENT_SUITE_NAME = "parentSuite"
    private const val ALLURE_DYNAMIC_SUB_SUITE_NAME = "subSuite"
    private const val ALLURE_DYNAMIC_PARAMETER = ALLURE_METHOD + "parameter"
    private const val ALLURE_DYNAMIC_ATTACHMENT_WRITE = ALLURE_METHOD + "addAttachment" + EVERYTHING_IN_PARENTHESES
    private const val VARIABLE = "[^'\",\\s)]+"
    private const val VALUE = "'[^']*'|\"[^\"]*\""
    private const val ASSIGNMENT = "\\s*=\\s*"

    private const val LINK_URL_PARAMETER_NAME = "url"
    private const val LINK_NAME_PARAMETER_NAME = "name"
    private const val LINK_PARAMETER_WITHOUT_NAME = "(?!" + LINK_URL_PARAMETER_NAME + ASSIGNMENT + "|" +
            LINK_NAME_PARAMETER_NAME + ASSIGNMENT + ")"
    private const val LINK_URL_PARAMETER = "(?:\\(\\s*" +
            "$LINK_PARAMETER_WITHOUT_NAME|(?<=$LINK_URL_PARAMETER_NAME)$ASSIGNMENT)" +
            "(?<$LINK_URL_PARAMETER_NAME>$VARIABLE|$VALUE)"
    private const val LINK_NAME_PARAMETER = "(?:" +
            "\\(\\s*(?:$VARIABLE|$VALUE)\\s*,\\s*$LINK_PARAMETER_WITHOUT_NAME|" +
            "(?<=$LINK_NAME_PARAMETER_NAME)$ASSIGNMENT)" +
            "(?<$LINK_NAME_PARAMETER_NAME>$VARIABLE|$VALUE)"

    private const val ATTACHMENT_CONTENT_PARAMETER_NAME = "content"
    private const val ATTACHMENT_SOURCE_PARAMETER_NAME = "source"
    private const val ATTACHMENT_NAME_PARAMETER_NAME = "name"
    private const val ATTACHMENT_TYPE_PARAMETER_NAME = "type"
    private const val ATTACHMENT_EXTENSION_PARAMETER_NAME = "fileExtension"
    private const val GENERAL_PARAMETER_WITHOUT_NAME = ATTACHMENT_NAME_PARAMETER_NAME + ASSIGNMENT + "|" +
            ATTACHMENT_TYPE_PARAMETER_NAME + ASSIGNMENT + "|" + ATTACHMENT_EXTENSION_PARAMETER_NAME + ASSIGNMENT
    private const val ATTACHMENT_READ_PARAMETER_WITHOUT_NAME = "(?!" + ATTACHMENT_SOURCE_PARAMETER_NAME + ASSIGNMENT +
            "|" + GENERAL_PARAMETER_WITHOUT_NAME + ")"
    private const val ATTACHMENT_WRITE_PARAMETER_WITHOUT_NAME = "(?!" + ATTACHMENT_CONTENT_PARAMETER_NAME + ASSIGNMENT +
            "|" + GENERAL_PARAMETER_WITHOUT_NAME + ")"
    private const val ATTACHMENT_CONTENT_PARAMETER = "(?:\\(\\s*" +
            "$ATTACHMENT_WRITE_PARAMETER_WITHOUT_NAME|(?<=$ATTACHMENT_CONTENT_PARAMETER_NAME)$ASSIGNMENT)" +
            "(?<$ATTACHMENT_CONTENT_PARAMETER_NAME>$VARIABLE|$VALUE)"
    private const val ATTACHMENT_NAME_PARAMETER = "(?:" +
            "\\(\\s*(?:$VARIABLE|$VALUE)\\s*,\\s*$ATTACHMENT_READ_PARAMETER_WITHOUT_NAME|" +
            "(?<=$ATTACHMENT_NAME_PARAMETER_NAME)$ASSIGNMENT)" +
            "(?<$ATTACHMENT_NAME_PARAMETER_NAME>$VARIABLE|$VALUE)"

    private const val PARAMETER_NAME_PARAMETER_NAME = "name"
    private const val PARAMETER_VALUE_PARAMETER_NAME = "value"
    private const val PARAMETER_PARAMETER_WITHOUT_NAME = "(?!" + PARAMETER_NAME_PARAMETER_NAME + ASSIGNMENT + "|" +
            PARAMETER_VALUE_PARAMETER_NAME + ASSIGNMENT + ")"
    private const val PARAMETER_NAME_PARAMETER = "(?:\\(\\s*" +
            "$PARAMETER_PARAMETER_WITHOUT_NAME|(?<=$PARAMETER_NAME_PARAMETER_NAME)$ASSIGNMENT)" +
            "(?<$PARAMETER_NAME_PARAMETER_NAME>$VARIABLE|$VALUE)"
    private const val PARAMETER_VALUE_PARAMETER = "(?:" +
            "\\(\\s*(?:$VARIABLE|$VALUE)\\s*,\\s*$PARAMETER_PARAMETER_WITHOUT_NAME|" +
            "(?<=$PARAMETER_VALUE_PARAMETER_NAME)$ASSIGNMENT)" +
            "(?<$PARAMETER_VALUE_PARAMETER_NAME>$VARIABLE|$VALUE)"

    private const val ANNOTATION_SEPARATOR_OBJECT = "."
    private const val PARAMETERS_SEPARATOR_OBJECT = ", "
    private const val TMS_OBJECT = "Adapter"
    private const val IMPORT_TMS = "import ru" + ANNOTATION_SEPARATOR_OBJECT + "testit" + ANNOTATION_SEPARATOR_OBJECT
    private const val IMPORT_TMS_ANNOTATIONS = IMPORT_TMS + "annotations" + ANNOTATION_SEPARATOR_OBJECT
    private const val IMPORT_TMS_SERVICES = IMPORT_TMS + "services" + ANNOTATION_SEPARATOR_OBJECT
    private const val TMS_DISPLAY_NAME_NAME = "DisplayName"
    private const val TMS_STEP_NAME = "Step"
    private const val IMPORT_TMS_DISPLAY_NAME = IMPORT_TMS_ANNOTATIONS + TMS_DISPLAY_NAME_NAME
    private const val IMPORT_TMS_DESCRIPTION = IMPORT_TMS_ANNOTATIONS + "Description"
    private const val IMPORT_TMS_STEP = IMPORT_TMS_ANNOTATIONS + TMS_STEP_NAME
    private const val JUPITER_TEST = ANNOTATION + "Test"
    private const val TMS_DISPLAY_NAME = ANNOTATION + TMS_DISPLAY_NAME_NAME
    private const val TMS_STEP = ANNOTATION + TMS_STEP_NAME
    private const val TMS_TITLE = ANNOTATION + "Title"
    private const val TMS_LABELS = ANNOTATION + "Labels"
    private const val TMS_LINK = ANNOTATION + "Link"
    private const val IMPORT_TMS_OBJECT = IMPORT_TMS_SERVICES + TMS_OBJECT
    private const val TMS_METHOD_OBJECT = TMS_OBJECT + ANNOTATION_SEPARATOR_OBJECT
    private const val TMS_NAMESPACE = TMS_METHOD_OBJECT + "nameSpace"
    private const val TMS_CLASSNAME = TMS_METHOD_OBJECT + "className"
    private const val TMS_ADD_NAMESPACE = TMS_METHOD_OBJECT + "addNameSpace"
    private const val TMS_ADD_CLASSNAME = TMS_METHOD_OBJECT + "addClassName"
    private const val TMS_ADD_DESCRIPTION = TMS_METHOD_OBJECT + "addDescription"
    private const val TMS_ADD_LABELS = TMS_METHOD_OBJECT + "addLabels"
    private const val TMS_ADD_LINKS = TMS_METHOD_OBJECT + "addLinks"
    private const val TMS_ADD_PARAMETER = TMS_METHOD_OBJECT + "addParameter"
    private const val TMS_ADD_ATTACHMENTS = TMS_METHOD_OBJECT + "addAttachments"

    // Compile patterns lazily once
    private val patternActions: Map<Regex, Any> by lazy {
        mutableMapOf(
            Regex(IMPORT_JUPITER_DISPLAY_NAME, RegexOption.MULTILINE) to IMPORT_TMS_DISPLAY_NAME,
            Regex(IMPORT_ALLURE_DESCRIPTION, RegexOption.MULTILINE) to IMPORT_TMS_DESCRIPTION,
            Regex(IMPORT_ALLURE_STEP, RegexOption.MULTILINE) to IMPORT_TMS_STEP,
            Regex(IMPORT_ALLURE_OBJECT, RegexOption.MULTILINE) to IMPORT_TMS_OBJECT,
            Regex(ALLURE_OWNER, RegexOption.MULTILINE) to TMS_LABELS,
            Regex(ALLURE_ID, RegexOption.MULTILINE) to TMS_LABELS,
            Regex(JUPITER_TEST_WITH_PARAMETERS, RegexOption.MULTILINE) to ::parseTestAnnotation,
            Regex(ALLURE_STEP_WITH_PARAMETERS, RegexOption.MULTILINE) to ::parseStepAnnotation,
            Regex(ALLURE_LINK, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(ALLURE_ISSUE, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(ALLURE_TMS_LINK, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(ALLURE_EPIC, RegexOption.MULTILINE) to TMS_NAMESPACE,
            Regex(ALLURE_FEATURE, RegexOption.MULTILINE) to TMS_NAMESPACE,
            Regex(ALLURE_STORY, RegexOption.MULTILINE) to TMS_CLASSNAME,
            Regex(ALLURE_DYNAMIC_DESCRIPTION, RegexOption.MULTILINE) to TMS_ADD_DESCRIPTION,
            Regex(ALLURE_DYNAMIC_DESCRIPTION_HTML, RegexOption.MULTILINE) to TMS_ADD_DESCRIPTION,
            Regex(ALLURE_DYNAMIC_LINK, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(ALLURE_DYNAMIC_ISSUE, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(ALLURE_DYNAMIC_TMS, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(ALLURE_DYNAMIC_LABEL, RegexOption.MULTILINE) to ::parseLabelMethod,
            Regex(ALLURE_DYNAMIC_EPIC, RegexOption.MULTILINE) to TMS_ADD_NAMESPACE,
            Regex(ALLURE_DYNAMIC_FEATURE, RegexOption.MULTILINE) to TMS_ADD_NAMESPACE,
            Regex(ALLURE_DYNAMIC_STORY, RegexOption.MULTILINE) to TMS_ADD_CLASSNAME,
            Regex(ALLURE_DYNAMIC_SUITE, RegexOption.MULTILINE) to TMS_ADD_NAMESPACE,
            Regex(ALLURE_DYNAMIC_ATTACHMENT_WRITE, RegexOption.MULTILINE) to ::parseWriteAttachMethod,
            Regex(ALLURE_DYNAMIC_PARAMETER, RegexOption.MULTILINE) to ::parseParameterMethod
        )
    }

    fun getPatterns(): List<Regex>
    {
        return patternActions.keys.toList()
    }

    fun parse(line: String, matchInfo: MatchInfo): String
    {
        for ((pattern, action) in patternActions) {
            if (pattern.matches(line)) {
                return when (action) {
                    is String -> action
                    is Function1<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val function = action as (MatchInfo) -> String
                        function(matchInfo)
                    }
                    // should be never called
                    else -> throw IllegalStateException("Unknown action type in patternActions")
                }
            }
        }
        throw Exception("No matching Allure pattern found in line \"$line\"")
    }

    private fun parseLinkAnnotation(matchInfo: MatchInfo): String
    {
        val urlMatch = Regex(LINK_URL_PARAMETER).find(matchInfo.text)
            ?: throw Exception("Can't getting url from annotation ${matchInfo.text}")
        val nameMatch = Regex(LINK_NAME_PARAMETER).find(matchInfo.text)

        val url = urlMatch.groups.get(LINK_URL_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(LINK_NAME_PARAMETER_NAME)?.value
        val titleBlock = if (name != null) "${PARAMETERS_SEPARATOR_OBJECT}title=${name}" else ""

        return "${TMS_LINK}(url=${url}${titleBlock})"
    }

    private fun parseTestAnnotation(matchInfo: MatchInfo): String
    {
        val valueMatch = Regex(VALUE).find(matchInfo.text)
            ?: throw Exception("Can't getting value from annotation ${matchInfo.text}")
        val value = valueMatch.value

        // Use string template for better readability
        return "$JUPITER_TEST\n$TMS_DISPLAY_NAME($value)"
    }

    private fun parseStepAnnotation(matchInfo: MatchInfo): String
    {
        val valueMatch = Regex(VALUE).find(matchInfo.text)
            ?: throw Exception("Can't getting value from annotation ${matchInfo.text}")
        val value = valueMatch.value

        // Use string template for better readability
        return "$TMS_STEP\n$TMS_TITLE($value)"
    }

    private fun parseLinkMethod(matchInfo: MatchInfo): String
    {
        val urlMatch = Regex(LINK_URL_PARAMETER).find(matchInfo.text)
            ?: throw Exception("Can't getting url from annotation ${matchInfo.text}")
        val nameMatch = Regex(LINK_NAME_PARAMETER).find(matchInfo.text)

        val url = urlMatch.groups.get(LINK_URL_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(LINK_NAME_PARAMETER_NAME)?.value
        val titleBlock = if (name != null) "${PARAMETERS_SEPARATOR_OBJECT}title=${name}" else ""

        return "${TMS_ADD_LINKS}(url=${url}${titleBlock})"
    }

    private fun parseWriteAttachMethod(matchInfo: MatchInfo): String
    {
        val contentMatch = Regex(ATTACHMENT_CONTENT_PARAMETER).find(matchInfo.text)
            ?: throw Exception("Can't getting content from method ${matchInfo.text}")
        val nameMatch = Regex(ATTACHMENT_NAME_PARAMETER).find(matchInfo.text)

        val content = contentMatch.groups.get(ATTACHMENT_CONTENT_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(ATTACHMENT_NAME_PARAMETER_NAME)?.value
        val nameBlock = if (name != null) "${PARAMETERS_SEPARATOR_OBJECT}name=${name}" else ""

        return "${TMS_ADD_ATTACHMENTS}(${content}${PARAMETERS_SEPARATOR_OBJECT}is_text=True${nameBlock})"
    }

    private fun parseParameterMethod(matchInfo: MatchInfo): String
    {
        val nameMatch = Regex(PARAMETER_NAME_PARAMETER).find(matchInfo.text)
            ?: throw Exception("Can't getting name from method ${matchInfo.text}")
        val valueMatch = Regex(PARAMETER_VALUE_PARAMETER).find(matchInfo.text)
            ?: throw Exception("Can't getting value from method ${matchInfo.text}")

        val name = nameMatch.groups.get(PARAMETER_NAME_PARAMETER_NAME)?.value
        val value = valueMatch.groups.get(PARAMETER_VALUE_PARAMETER_NAME)?.value

        // Use string template for better readability
        return "${TMS_ADD_PARAMETER}(name=$name${PARAMETERS_SEPARATOR_OBJECT}value=$value)"
    }

    private fun parseLabelMethod(matchInfo: MatchInfo): String
    {
        val nameMatch = Regex(PARAMETER_NAME_PARAMETER).find(matchInfo.text)
            ?: throw Exception("Can't getting name from method ${matchInfo.text}")
        val valueMatch = Regex(PARAMETER_VALUE_PARAMETER).find(matchInfo.text)
            ?: throw Exception("Can't getting value from method ${matchInfo.text}")

        val name = nameMatch.groups.get(PARAMETER_NAME_PARAMETER_NAME)?.value
        val value = valueMatch.groups.get(PARAMETER_VALUE_PARAMETER_NAME)?.value

        if (name == "\"$ALLURE_DYNAMIC_PARENT_SUITE_NAME\"") {
            // Use string template for better readability
            return "${TMS_ADD_NAMESPACE}($value)"
        }

        if (name == "\"$ALLURE_DYNAMIC_SUB_SUITE_NAME\"") {
            // Use string template for better readability
            return "${TMS_ADD_CLASSNAME}($value)"
        }

        // Use string template for better readability
        return "${TMS_ADD_LABELS}(${name?.dropLast(1)}:${value?.drop(1)})"
    }
}