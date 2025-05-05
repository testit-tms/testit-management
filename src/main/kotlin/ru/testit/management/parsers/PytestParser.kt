package ru.testit.management.parsers

import ru.testit.management.parsers.models.MatchInfo
import java.util.regex.Pattern

// TODO: move all regex patterns to the separate file
// TODO: move all objects to the separate file
object PytestParser {
    private const val ANNOTATION_SEPARATOR = "\\."
    private const val EVERYTHING_IN_PARENTHESES = "\\([\\s\\S][^)]{1,}\\)"
    private const val ALLURE_OBJECT = "allure"
    private const val ALLURE_METHOD = ALLURE_OBJECT + ANNOTATION_SEPARATOR
    private const val IMPORT_ALLURE_OBJECT = "import $ALLURE_OBJECT"
    private const val ALLURE_TITLE = ALLURE_METHOD + "title"
    private const val ALLURE_DESCRIPTION = ALLURE_METHOD + "description"
    private const val ALLURE_DESCRIPTION_HTML = ALLURE_METHOD + "description_html"
    private const val ALLURE_LINK = ALLURE_METHOD + "link" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_ISSUE = ALLURE_METHOD + "issue" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_TESTCASE = ALLURE_METHOD + "testcase" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_TAG = ALLURE_METHOD + "tag"
    private const val ALLURE_LABEL = ALLURE_METHOD + "label"
    private const val ALLURE_ID = ALLURE_METHOD + "id"
    private const val ALLURE_EPIC = ALLURE_METHOD + "epic"
    private const val ALLURE_FEATURE = ALLURE_METHOD + "feature"
    private const val ALLURE_STORY = ALLURE_METHOD + "story"
    private const val ALLURE_PARENT_SUITE = ALLURE_METHOD + "parent_suite"
    private const val ALLURE_SUITE = ALLURE_METHOD + "suite"
    private const val ALLURE_SUB_SUITE = ALLURE_METHOD + "sub_suite"
    private const val ALLURE_STEP = ALLURE_METHOD + "step"
    private const val ALLURE_DYNAMIC = ALLURE_METHOD + "dynamic" + ANNOTATION_SEPARATOR
    private const val ALLURE_DYNAMIC_TITLE = ALLURE_DYNAMIC + "title"
    private const val ALLURE_DYNAMIC_DESCRIPTION = ALLURE_DYNAMIC + "description"
    private const val ALLURE_DYNAMIC_DESCRIPTION_HTML = ALLURE_DYNAMIC + "description_html"
    private const val ALLURE_DYNAMIC_LINK = ALLURE_DYNAMIC + "link" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_DYNAMIC_ISSUE = ALLURE_DYNAMIC + "issue" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_DYNAMIC_TESTCASES = ALLURE_DYNAMIC + "testcase" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_DYNAMIC_TAG = ALLURE_DYNAMIC + "tag"
    private const val ALLURE_DYNAMIC_LABEL = ALLURE_DYNAMIC + "label"
    private const val ALLURE_DYNAMIC_ID = ALLURE_DYNAMIC + "id"
    private const val ALLURE_DYNAMIC_EPIC = ALLURE_DYNAMIC + "epic"
    private const val ALLURE_DYNAMIC_FEATURE = ALLURE_DYNAMIC + "feature"
    private const val ALLURE_DYNAMIC_STORY = ALLURE_DYNAMIC + "story"
    private const val ALLURE_DYNAMIC_PARENT_SUITE = ALLURE_DYNAMIC + "parent_suite"
    private const val ALLURE_DYNAMIC_SUITE = ALLURE_DYNAMIC + "suite"
    private const val ALLURE_DYNAMIC_SUB_SUITE = ALLURE_DYNAMIC + "sub_suite"
    private const val ALLURE_DYNAMIC_PARAMETER = ALLURE_DYNAMIC + "parameter"
    private const val ALLURE_DYNAMIC_ATTACHMENT_WRITE = ALLURE_METHOD + "attach" + EVERYTHING_IN_PARENTHESES
    private const val ALLURE_DYNAMIC_ATTACHMENT_READ = ALLURE_METHOD + "attach" + ANNOTATION_SEPARATOR +
            "file" + EVERYTHING_IN_PARENTHESES
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

    private const val ATTACHMENT_BODY_PARAMETER_NAME = "body"
    private const val ATTACHMENT_SOURCE_PARAMETER_NAME = "source"
    private const val ATTACHMENT_NAME_PARAMETER_NAME = "name"
    private const val ATTACHMENT_TYPE_PARAMETER_NAME = "attachment_type"
    private const val ATTACHMENT_EXTENSION_PARAMETER_NAME = "extension"
    private const val GENERAL_PARAMETER_WITHOUT_NAME = ATTACHMENT_NAME_PARAMETER_NAME + ASSIGNMENT + "|" +
            ATTACHMENT_TYPE_PARAMETER_NAME + ASSIGNMENT + "|" + ATTACHMENT_EXTENSION_PARAMETER_NAME + ASSIGNMENT
    private const val ATTACHMENT_READ_PARAMETER_WITHOUT_NAME = "(?!" + ATTACHMENT_SOURCE_PARAMETER_NAME + ASSIGNMENT +
            "|" + GENERAL_PARAMETER_WITHOUT_NAME + ")"
    private const val ATTACHMENT_WRITE_PARAMETER_WITHOUT_NAME = "(?!" + ATTACHMENT_BODY_PARAMETER_NAME + ASSIGNMENT +
            "|" + GENERAL_PARAMETER_WITHOUT_NAME + ")"
    private const val ATTACHMENT_SOURCE_PARAMETER = "(?:\\(\\s*" +
            "$ATTACHMENT_READ_PARAMETER_WITHOUT_NAME|(?<=$ATTACHMENT_SOURCE_PARAMETER_NAME)$ASSIGNMENT)" +
            "(?<$ATTACHMENT_SOURCE_PARAMETER_NAME>$VARIABLE|$VALUE)"
    private const val ATTACHMENT_BODY_PARAMETER = "(?:\\(\\s*" +
            "$ATTACHMENT_WRITE_PARAMETER_WITHOUT_NAME|(?<=$ATTACHMENT_BODY_PARAMETER_NAME)$ASSIGNMENT)" +
            "(?<$ATTACHMENT_BODY_PARAMETER_NAME>$VARIABLE|$VALUE)"
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
    private const val TMS_OBJECT = "testit"
    private const val IMPORT_TMS_OBJECT = "import $TMS_OBJECT"
    private const val TMS_METHOD_OBJECT = TMS_OBJECT + ANNOTATION_SEPARATOR_OBJECT
    private const val TMS_DISPLAY_NAME = TMS_METHOD_OBJECT + "displayName"
    private const val TMS_DESCRIPTION = TMS_METHOD_OBJECT + "description"
    private const val TMS_LABELS = TMS_METHOD_OBJECT + "labels"
    private const val TMS_STEP = TMS_METHOD_OBJECT + "step"
    private const val TMS_LINKS = TMS_METHOD_OBJECT + "links"
    private const val TMS_NAMESPACE = TMS_METHOD_OBJECT + "nameSpace"
    private const val TMS_CLASSNAME = TMS_METHOD_OBJECT + "className"
    private const val TMS_ADD_DISPLAY_NAME = TMS_METHOD_OBJECT + "addDisplayName"
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
            Regex(IMPORT_ALLURE_OBJECT, RegexOption.MULTILINE) to IMPORT_TMS_OBJECT,
            Regex(ALLURE_TITLE, RegexOption.MULTILINE) to TMS_DISPLAY_NAME,
            Regex(ALLURE_DESCRIPTION, RegexOption.MULTILINE) to TMS_DESCRIPTION,
            Regex(ALLURE_DESCRIPTION_HTML, RegexOption.MULTILINE) to TMS_DESCRIPTION,
            Regex(ALLURE_TAG, RegexOption.MULTILINE) to TMS_LABELS,
            Regex(ALLURE_LABEL, RegexOption.MULTILINE) to TMS_LABELS,
            Regex(ALLURE_ID, RegexOption.MULTILINE) to TMS_LABELS,
            Regex(ALLURE_STEP, RegexOption.MULTILINE) to TMS_STEP,
            Regex(ALLURE_LINK, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(ALLURE_ISSUE, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(ALLURE_TESTCASE, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(ALLURE_PARENT_SUITE, RegexOption.MULTILINE) to TMS_NAMESPACE,
            Regex(ALLURE_SUITE, RegexOption.MULTILINE) to TMS_NAMESPACE,
            Regex(ALLURE_SUB_SUITE, RegexOption.MULTILINE) to TMS_CLASSNAME,
            Regex(ALLURE_EPIC, RegexOption.MULTILINE) to TMS_NAMESPACE,
            Regex(ALLURE_FEATURE, RegexOption.MULTILINE) to TMS_NAMESPACE,
            Regex(ALLURE_STORY, RegexOption.MULTILINE) to TMS_CLASSNAME,
            Regex(ALLURE_DYNAMIC_TITLE, RegexOption.MULTILINE) to TMS_ADD_DISPLAY_NAME,
            Regex(ALLURE_DYNAMIC_DESCRIPTION, RegexOption.MULTILINE) to TMS_ADD_DESCRIPTION,
            Regex(ALLURE_DYNAMIC_DESCRIPTION_HTML, RegexOption.MULTILINE) to TMS_ADD_DESCRIPTION,
            Regex(ALLURE_DYNAMIC_LINK, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(ALLURE_DYNAMIC_ISSUE, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(ALLURE_DYNAMIC_TESTCASES, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(ALLURE_DYNAMIC_TAG, RegexOption.MULTILINE) to TMS_ADD_LABELS,
            Regex(ALLURE_DYNAMIC_LABEL, RegexOption.MULTILINE) to TMS_ADD_LABELS,
            Regex(ALLURE_DYNAMIC_ID, RegexOption.MULTILINE) to TMS_ADD_LABELS,
            Regex(ALLURE_DYNAMIC_EPIC, RegexOption.MULTILINE) to TMS_ADD_NAMESPACE,
            Regex(ALLURE_DYNAMIC_FEATURE, RegexOption.MULTILINE) to TMS_ADD_NAMESPACE,
            Regex(ALLURE_DYNAMIC_STORY, RegexOption.MULTILINE) to TMS_ADD_CLASSNAME,
            Regex(ALLURE_DYNAMIC_PARENT_SUITE, RegexOption.MULTILINE) to TMS_ADD_NAMESPACE,
            Regex(ALLURE_DYNAMIC_SUITE, RegexOption.MULTILINE) to TMS_ADD_NAMESPACE,
            Regex(ALLURE_DYNAMIC_SUB_SUITE, RegexOption.MULTILINE) to TMS_ADD_CLASSNAME,
            Regex(ALLURE_DYNAMIC_ATTACHMENT_WRITE, RegexOption.MULTILINE) to ::parseWriteAttachMethod,
            Regex(ALLURE_DYNAMIC_ATTACHMENT_READ, RegexOption.MULTILINE) to ::parseReadAttachMethod,
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

        return "${TMS_LINKS}(url=${url}${titleBlock})"
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
        val bodyMatch = Regex(ATTACHMENT_BODY_PARAMETER).find(matchInfo.text)
            ?: throw Exception("Can't getting body from method ${matchInfo.text}")
        val nameMatch = Regex(ATTACHMENT_NAME_PARAMETER).find(matchInfo.text)

        val body = bodyMatch.groups.get(ATTACHMENT_BODY_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(ATTACHMENT_NAME_PARAMETER_NAME)?.value
        val nameBlock = if (name != null) "${PARAMETERS_SEPARATOR_OBJECT}name=${name}" else ""

        return "${TMS_ADD_ATTACHMENTS}(${body}${PARAMETERS_SEPARATOR_OBJECT}is_text=True${nameBlock})"
    }

    private fun parseReadAttachMethod(matchInfo: MatchInfo): String
    {
        val sourceMatch = Regex(ATTACHMENT_SOURCE_PARAMETER).find(matchInfo.text)
            ?: throw Exception("Can't getting source from method ${matchInfo.text}")
        val nameMatch = Regex(ATTACHMENT_NAME_PARAMETER).find(matchInfo.text)

        val source = sourceMatch.groups.get(ATTACHMENT_SOURCE_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(ATTACHMENT_NAME_PARAMETER_NAME)?.value
        val nameBlock = if (name != null) "${PARAMETERS_SEPARATOR_OBJECT}name=${name}" else ""

        return "${TMS_ADD_ATTACHMENTS}(${source}${nameBlock})"
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
}