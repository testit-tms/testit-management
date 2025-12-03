package ru.testit.management.parsers

import ru.testit.management.parsers.regexps.RobotFrameworkRegexp

object RobotFrameworkParser {
    // Compile patterns lazily once
    private val patternActions: Map<Regex, Any> by lazy {
        mutableMapOf(
            Regex(RobotFrameworkRegexp.IMPORT_ALLURE_OBJECT, RegexOption.MULTILINE) to RobotFrameworkRegexp.IMPORT_TMS_OBJECT,
            Regex(RobotFrameworkRegexp.ALLURE_OTHER_FUNCTIONS_LABELS, RegexOption.MULTILINE) to ::parseOtherFunctionsLabels,
            Regex(RobotFrameworkRegexp.ALLURE_STEP, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_STEP,
            Regex(RobotFrameworkRegexp.ALLURE_LINK, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(RobotFrameworkRegexp.ALLURE_ISSUE, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(RobotFrameworkRegexp.ALLURE_TESTCASE, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(RobotFrameworkRegexp.ALLURE_PARENT_SUITE, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_NAMESPACE,
            Regex(RobotFrameworkRegexp.ALLURE_SUITE, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_NAMESPACE,
            Regex(RobotFrameworkRegexp.ALLURE_SUB_SUITE, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_CLASSNAME,
            Regex(RobotFrameworkRegexp.ALLURE_EPIC, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_NAMESPACE,
            Regex(RobotFrameworkRegexp.ALLURE_FEATURE, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_NAMESPACE,
            Regex(RobotFrameworkRegexp.ALLURE_STORY, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_CLASSNAME,
            Regex(RobotFrameworkRegexp.ALLURE_PACKAGE, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_NAMESPACE,
            Regex(RobotFrameworkRegexp.ALLURE_TEST_CLASS, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_CLASSNAME,
            Regex(RobotFrameworkRegexp.ALLURE_TEST_METHOD, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_DISPLAY_NAME,
            Regex(RobotFrameworkRegexp.ALLURE_ID, RegexOption.MULTILINE) to ::parseIdLabel,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_TITLE, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_DISPLAY_NAME,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_DESCRIPTION, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_DESCRIPTION,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_DESCRIPTION_HTML, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_DESCRIPTION,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_LINK, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_ISSUE, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_TESTCASES, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_TAG, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_LABELS,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_LABEL, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_LABELS,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_ID, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_LABELS,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_EPIC, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_NAMESPACE,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_FEATURE, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_NAMESPACE,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_STORY, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_CLASSNAME,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_PARENT_SUITE, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_NAMESPACE,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_SUITE, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_NAMESPACE,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_SUB_SUITE, RegexOption.MULTILINE) to RobotFrameworkRegexp.TMS_ADD_CLASSNAME,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_ATTACHMENT_WRITE, RegexOption.MULTILINE) to ::parseWriteAttachMethod,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_ATTACHMENT_READ, RegexOption.MULTILINE) to ::parseReadAttachMethod,
            Regex(RobotFrameworkRegexp.ALLURE_DYNAMIC_PARAMETER, RegexOption.MULTILINE) to ::parseParameterMethod
        )
    }

    fun getPatterns(): List<Regex>
    {
        return patternActions.keys.toList()
    }

    fun parse(content: String): String
    {
        for ((pattern, action) in patternActions) {
            if (pattern.matches(content)) {
                return when (action) {
                    is String -> action
                    is Function1<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val function = action as (String) -> String
                        function(content)
                    }
                    // should be never called
                    else -> throw IllegalStateException("Unknown action type in patternActions")
                }
            }
        }
        throw Exception("No matching Allure pattern found in line \"$content\"")
    }

    private fun parseOtherFunctionsLabels(content: String): String
    {
        val valueMatch = Regex(RobotFrameworkRegexp.LABEL_VALUE_ANNOTATION).find(content)
            ?: throw Exception("Can't getting value from annotation \"$content\"")

        val name = valueMatch.groups.get(RobotFrameworkRegexp.LABEL_VALUE_ANNOTATION_NAME)?.value

        return "${RobotFrameworkRegexp.TMS_LABELS}\${{['$name']}}"
    }

    private fun parseIdLabel(content: String): String
    {
        val valueMatch = Regex(RobotFrameworkRegexp.LABEL_ID_VALUE_ANNOTATION).find(content)
            ?: throw Exception("Can't getting value from annotation \"$content\"")

        val name = valueMatch.groups.get(RobotFrameworkRegexp.LABEL_VALUE_ANNOTATION_NAME)?.value

        return "${RobotFrameworkRegexp.TMS_LABELS}\${{['$name']}}"
    }

    private fun parseLinkAnnotation(content: String): String
    {
        val urlMatch = Regex(RobotFrameworkRegexp.LINK_URL_ANNOTATION).find(content)
            ?: throw Exception("Can't getting url from annotation \"$content\"")
        val nameMatch = Regex(RobotFrameworkRegexp.LINK_NAME_ANNOTATION).find(content)

        val url = urlMatch.groups.get(RobotFrameworkRegexp.LINK_URL_ANNOTATION_NAME)?.value
        val name = nameMatch?.groups?.get(RobotFrameworkRegexp.LINK_NAME_ANNOTATION_NAME)?.value
        val titleBlock = if (name != null) "${RobotFrameworkRegexp.ANNOTATION_SEPARATOR_OBJECT}\"title\":\"$name\"" else ""
        return "${RobotFrameworkRegexp.TMS_LINKS}\${{{\"url\":\"$url\"$titleBlock}}}"
    }

    private fun parseLinkMethod(content: String): String
    {
        val urlMatch = Regex(RobotFrameworkRegexp.LINK_URL_PARAMETER).find(content)
            ?: throw Exception("Can't getting url from annotation \"$content\"")
        val nameMatch = Regex(RobotFrameworkRegexp.LINK_NAME_PARAMETER).find(content)

        val url = urlMatch.groups.get(RobotFrameworkRegexp.LINK_URL_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(RobotFrameworkRegexp.LINK_NAME_PARAMETER_NAME)?.value
        val titleBlock = if (name != null) "${RobotFrameworkRegexp.PARAMETERS_SEPARATOR_OBJECT}title=${name}" else ""

        return "${RobotFrameworkRegexp.TMS_ADD_LINKS}(url=${url}${titleBlock})"
    }

    private fun parseWriteAttachMethod(content: String): String
    {
        val bodyMatch = Regex(RobotFrameworkRegexp.ATTACHMENT_BODY_PARAMETER).find(content)
            ?: throw Exception("Can't getting body from method \"$content\"")
        val nameMatch = Regex(RobotFrameworkRegexp.ATTACHMENT_NAME_PARAMETER).find(content)

        val body = bodyMatch.groups.get(RobotFrameworkRegexp.ATTACHMENT_BODY_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(RobotFrameworkRegexp.ATTACHMENT_NAME_PARAMETER_NAME)?.value
        val nameBlock = if (name != null) "${RobotFrameworkRegexp.PARAMETERS_SEPARATOR_OBJECT}name=${name}" else ""

        return "${RobotFrameworkRegexp.TMS_ADD_ATTACHMENTS}(${body}${RobotFrameworkRegexp.PARAMETERS_SEPARATOR_OBJECT}is_text=True${nameBlock})"
    }

    private fun parseReadAttachMethod(content: String): String
    {
        val sourceMatch = Regex(RobotFrameworkRegexp.ATTACHMENT_SOURCE_PARAMETER).find(content)
            ?: throw Exception("Can't getting source from method \"$content\"")
        val nameMatch = Regex(RobotFrameworkRegexp.ATTACHMENT_NAME_PARAMETER).find(content)

        val source = sourceMatch.groups.get(RobotFrameworkRegexp.ATTACHMENT_SOURCE_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(RobotFrameworkRegexp.ATTACHMENT_NAME_PARAMETER_NAME)?.value
        val nameBlock = if (name != null) "${RobotFrameworkRegexp.PARAMETERS_SEPARATOR_OBJECT}name=${name}" else ""

        return "${RobotFrameworkRegexp.TMS_ADD_ATTACHMENTS}(${source}${nameBlock})"
    }

    private fun parseParameterMethod(content: String): String
    {
        val nameMatch = Regex(RobotFrameworkRegexp.PARAMETER_NAME_PARAMETER).find(content)
            ?: throw Exception("Can't getting name from method \"$content\"")
        val valueMatch = Regex(RobotFrameworkRegexp.PARAMETER_VALUE_PARAMETER).find(content)
            ?: throw Exception("Can't getting value from method \"$content\"")

        val name = nameMatch.groups.get(RobotFrameworkRegexp.PARAMETER_NAME_PARAMETER_NAME)?.value
        val value = valueMatch.groups.get(RobotFrameworkRegexp.PARAMETER_VALUE_PARAMETER_NAME)?.value

        // Use string template for better readability
        return "${RobotFrameworkRegexp.TMS_ADD_PARAMETER}(name=$name${RobotFrameworkRegexp.PARAMETERS_SEPARATOR_OBJECT}value=$value)"
    }
}
