package ru.testit.management.parsers

import ru.testit.management.parsers.regexps.PytestRegexp

object PytestParser {
    // Compile patterns lazily once
    private val patternActions: Map<Regex, Any> by lazy {
        mutableMapOf(
            Regex(PytestRegexp.IMPORT_ALLURE_OBJECT, RegexOption.MULTILINE) to PytestRegexp.IMPORT_TMS_OBJECT,
            Regex(PytestRegexp.ALLURE_TITLE, RegexOption.MULTILINE) to PytestRegexp.TMS_DISPLAY_NAME,
            Regex(PytestRegexp.ALLURE_DESCRIPTION, RegexOption.MULTILINE) to PytestRegexp.TMS_DESCRIPTION,
            Regex(PytestRegexp.ALLURE_DESCRIPTION_HTML, RegexOption.MULTILINE) to PytestRegexp.TMS_DESCRIPTION,
            Regex(PytestRegexp.ALLURE_TAG, RegexOption.MULTILINE) to PytestRegexp.TMS_LABELS,
            Regex(PytestRegexp.ALLURE_LABEL, RegexOption.MULTILINE) to PytestRegexp.TMS_LABELS,
            Regex(PytestRegexp.ALLURE_ID, RegexOption.MULTILINE) to PytestRegexp.TMS_LABELS,
            Regex(PytestRegexp.ALLURE_STEP, RegexOption.MULTILINE) to PytestRegexp.TMS_STEP,
            Regex(PytestRegexp.ALLURE_LINK, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(PytestRegexp.ALLURE_ISSUE, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(PytestRegexp.ALLURE_TESTCASE, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(PytestRegexp.ALLURE_PARENT_SUITE, RegexOption.MULTILINE) to PytestRegexp.TMS_NAMESPACE,
            Regex(PytestRegexp.ALLURE_SUITE, RegexOption.MULTILINE) to PytestRegexp.TMS_NAMESPACE,
            Regex(PytestRegexp.ALLURE_SUB_SUITE, RegexOption.MULTILINE) to PytestRegexp.TMS_CLASSNAME,
            Regex(PytestRegexp.ALLURE_EPIC, RegexOption.MULTILINE) to PytestRegexp.TMS_NAMESPACE,
            Regex(PytestRegexp.ALLURE_FEATURE, RegexOption.MULTILINE) to PytestRegexp.TMS_NAMESPACE,
            Regex(PytestRegexp.ALLURE_STORY, RegexOption.MULTILINE) to PytestRegexp.TMS_CLASSNAME,
            Regex(PytestRegexp.ALLURE_DYNAMIC_TITLE, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_DISPLAY_NAME,
            Regex(PytestRegexp.ALLURE_DYNAMIC_DESCRIPTION, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_DESCRIPTION,
            Regex(PytestRegexp.ALLURE_DYNAMIC_DESCRIPTION_HTML, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_DESCRIPTION,
            Regex(PytestRegexp.ALLURE_DYNAMIC_LINK, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(PytestRegexp.ALLURE_DYNAMIC_ISSUE, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(PytestRegexp.ALLURE_DYNAMIC_TESTCASES, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(PytestRegexp.ALLURE_DYNAMIC_TAG, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_LABELS,
            Regex(PytestRegexp.ALLURE_DYNAMIC_LABEL, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_LABELS,
            Regex(PytestRegexp.ALLURE_DYNAMIC_ID, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_LABELS,
            Regex(PytestRegexp.ALLURE_DYNAMIC_EPIC, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_NAMESPACE,
            Regex(PytestRegexp.ALLURE_DYNAMIC_FEATURE, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_NAMESPACE,
            Regex(PytestRegexp.ALLURE_DYNAMIC_STORY, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_CLASSNAME,
            Regex(PytestRegexp.ALLURE_DYNAMIC_PARENT_SUITE, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_NAMESPACE,
            Regex(PytestRegexp.ALLURE_DYNAMIC_SUITE, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_NAMESPACE,
            Regex(PytestRegexp.ALLURE_DYNAMIC_SUB_SUITE, RegexOption.MULTILINE) to PytestRegexp.TMS_ADD_CLASSNAME,
            Regex(PytestRegexp.ALLURE_DYNAMIC_ATTACHMENT_WRITE, RegexOption.MULTILINE) to ::parseWriteAttachMethod,
            Regex(PytestRegexp.ALLURE_DYNAMIC_ATTACHMENT_READ, RegexOption.MULTILINE) to ::parseReadAttachMethod,
            Regex(PytestRegexp.ALLURE_DYNAMIC_PARAMETER, RegexOption.MULTILINE) to ::parseParameterMethod
        )
    }

    fun getPatterns(): List<Regex>
    {
        return patternActions.keys.toList()
    }

    fun parse(code: String): String
    {
        for ((pattern, action) in patternActions) {
            if (pattern.matches(code)) {
                return when (action) {
                    is String -> action
                    is Function1<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val function = action as (String) -> String
                        function(code)
                    }
                    // should be never called
                    else -> throw IllegalStateException("Unknown action type in patternActions") 
                }
            }
        }
        throw Exception("No matching Allure pattern found in line \"$code\"")
    }

    private fun parseLinkAnnotation(code: String): String
    {
        val urlMatch = Regex(PytestRegexp.LINK_URL_PARAMETER).find(code)
            ?: throw Exception("Can't getting url from annotation \"$code\"")
        val nameMatch = Regex(PytestRegexp.LINK_NAME_PARAMETER).find(code)

        val url = urlMatch.groups.get(PytestRegexp.LINK_URL_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(PytestRegexp.LINK_NAME_PARAMETER_NAME)?.value
        val titleBlock = if (name != null) "${PytestRegexp.PARAMETERS_SEPARATOR_OBJECT}title=${name}" else ""

        return "${PytestRegexp.TMS_LINKS}(url=${url}${titleBlock})"
    }

    private fun parseLinkMethod(code: String): String
    {
        val urlMatch = Regex(PytestRegexp.LINK_URL_PARAMETER).find(code)
            ?: throw Exception("Can't getting url from annotation \"$code\"")
        val nameMatch = Regex(PytestRegexp.LINK_NAME_PARAMETER).find(code)

        val url = urlMatch.groups.get(PytestRegexp.LINK_URL_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(PytestRegexp.LINK_NAME_PARAMETER_NAME)?.value
        val titleBlock = if (name != null) "${PytestRegexp.PARAMETERS_SEPARATOR_OBJECT}title=${name}" else ""

        return "${PytestRegexp.TMS_ADD_LINKS}(url=${url}${titleBlock})"
    }

    private fun parseWriteAttachMethod(code: String): String
    {
        val bodyMatch = Regex(PytestRegexp.ATTACHMENT_BODY_PARAMETER).find(code)
            ?: throw Exception("Can't getting body from method \"$code\"")
        val nameMatch = Regex(PytestRegexp.ATTACHMENT_NAME_PARAMETER).find(code)

        val body = bodyMatch.groups.get(PytestRegexp.ATTACHMENT_BODY_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(PytestRegexp.ATTACHMENT_NAME_PARAMETER_NAME)?.value
        val nameBlock = if (name != null) "${PytestRegexp.PARAMETERS_SEPARATOR_OBJECT}name=${name}" else ""

        return "${PytestRegexp.TMS_ADD_ATTACHMENTS}(${body}${PytestRegexp.PARAMETERS_SEPARATOR_OBJECT}is_text=True${nameBlock})"
    }

    private fun parseReadAttachMethod(code: String): String
    {
        val sourceMatch = Regex(PytestRegexp.ATTACHMENT_SOURCE_PARAMETER).find(code)
            ?: throw Exception("Can't getting source from method \"$code\"")
        val nameMatch = Regex(PytestRegexp.ATTACHMENT_NAME_PARAMETER).find(code)

        val source = sourceMatch.groups.get(PytestRegexp.ATTACHMENT_SOURCE_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(PytestRegexp.ATTACHMENT_NAME_PARAMETER_NAME)?.value
        val nameBlock = if (name != null) "${PytestRegexp.PARAMETERS_SEPARATOR_OBJECT}name=${name}" else ""

        return "${PytestRegexp.TMS_ADD_ATTACHMENTS}(${source}${nameBlock})"
    }

    private fun parseParameterMethod(code: String): String
    {
        val nameMatch = Regex(PytestRegexp.PARAMETER_NAME_PARAMETER).find(code)
            ?: throw Exception("Can't getting name from method \"$code\"")
        val valueMatch = Regex(PytestRegexp.PARAMETER_VALUE_PARAMETER).find(code)
            ?: throw Exception("Can't getting value from method \"$code\"")

        val name = nameMatch.groups.get(PytestRegexp.PARAMETER_NAME_PARAMETER_NAME)?.value
        val value = valueMatch.groups.get(PytestRegexp.PARAMETER_VALUE_PARAMETER_NAME)?.value

        // Use string template for better readability
        return "${PytestRegexp.TMS_ADD_PARAMETER}(name=$name${PytestRegexp.PARAMETERS_SEPARATOR_OBJECT}value=$value)"
    }
}
