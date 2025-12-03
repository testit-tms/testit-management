package ru.testit.management.parsers

import ru.testit.management.parsers.regexps.JUnitRegexp

object JUnitParser {
    // Compile patterns lazily once
    private val patternActions: Map<Regex, Any> by lazy {
        mutableMapOf(
            Regex(JUnitRegexp.IMPORT_JUPITER_DISPLAY_NAME, RegexOption.MULTILINE) to JUnitRegexp.IMPORT_TMS_DISPLAY_NAME,
            Regex(JUnitRegexp.IMPORT_ALLURE_DESCRIPTION, RegexOption.MULTILINE) to JUnitRegexp.IMPORT_TMS_DESCRIPTION,
            Regex(JUnitRegexp.IMPORT_ALLURE_STEP, RegexOption.MULTILINE) to JUnitRegexp.IMPORT_TMS_STEP,
            Regex(JUnitRegexp.IMPORT_ALLURE_OBJECT, RegexOption.MULTILINE) to JUnitRegexp.IMPORT_TMS_OBJECT,
            Regex(JUnitRegexp.ALLURE_OWNER, RegexOption.MULTILINE) to JUnitRegexp.TMS_LABELS,
            Regex(JUnitRegexp.ALLURE_ID, RegexOption.MULTILINE) to JUnitRegexp.TMS_LABELS,
            Regex(JUnitRegexp.JUPITER_TEST_WITH_PARAMETERS, RegexOption.MULTILINE) to ::parseTestAnnotation,
            Regex(JUnitRegexp.ALLURE_STEP_WITH_PARAMETERS, RegexOption.MULTILINE) to ::parseStepAnnotation,
            Regex(JUnitRegexp.ALLURE_LINK, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(JUnitRegexp.ALLURE_ISSUE, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(JUnitRegexp.ALLURE_TMS_LINK, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(JUnitRegexp.ALLURE_EPIC, RegexOption.MULTILINE) to JUnitRegexp.TMS_NAMESPACE,
            Regex(JUnitRegexp.ALLURE_FEATURE, RegexOption.MULTILINE) to JUnitRegexp.TMS_NAMESPACE,
            Regex(JUnitRegexp.ALLURE_STORY, RegexOption.MULTILINE) to JUnitRegexp.TMS_CLASSNAME,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_DESCRIPTION, RegexOption.MULTILINE) to JUnitRegexp.TMS_ADD_DESCRIPTION,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_DESCRIPTION_HTML, RegexOption.MULTILINE) to JUnitRegexp.TMS_ADD_DESCRIPTION,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_LINK, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_ISSUE, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_TMS, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_LABEL, RegexOption.MULTILINE) to ::parseLabelMethod,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_EPIC, RegexOption.MULTILINE) to JUnitRegexp.TMS_ADD_NAMESPACE,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_FEATURE, RegexOption.MULTILINE) to JUnitRegexp.TMS_ADD_NAMESPACE,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_STORY, RegexOption.MULTILINE) to JUnitRegexp.TMS_ADD_CLASSNAME,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_SUITE, RegexOption.MULTILINE) to JUnitRegexp.TMS_ADD_NAMESPACE,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_ATTACHMENT_WRITE, RegexOption.MULTILINE) to ::parseWriteAttachMethod,
            Regex(JUnitRegexp.ALLURE_DYNAMIC_PARAMETER, RegexOption.MULTILINE) to ::parseParameterMethod
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

    private fun parseLinkAnnotation(content: String): String
    {
        val urlMatch = Regex(JUnitRegexp.LINK_URL_PARAMETER).find(content)
            ?: throw Exception("Can't getting url from annotation \"$content\"")
        val nameMatch = Regex(JUnitRegexp.LINK_NAME_PARAMETER).find(content)

        val url = urlMatch.groups.get(JUnitRegexp.LINK_URL_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(JUnitRegexp.LINK_NAME_PARAMETER_NAME)?.value
        val titleBlock = if (name != null) "${JUnitRegexp.PARAMETERS_SEPARATOR_OBJECT}title=${name}" else ""

        return "${JUnitRegexp.TMS_LINK}(url=${url}${titleBlock})"
    }

    private fun parseTestAnnotation(content: String): String
    {
        val valueMatch = Regex(JUnitRegexp.VALUE).find(content)
            ?: throw Exception("Can't getting value from annotation \"$content\"")
        val value = valueMatch.value

        // Use string template for better readability
        return "${JUnitRegexp.JUPITER_TEST}\n${JUnitRegexp.TMS_DISPLAY_NAME}($value)"
    }

    fun parseStepAnnotation(content: String): String
    {
        val valueMatch = Regex(JUnitRegexp.VALUE).find(content)
            ?: throw Exception("Can't getting value from annotation \"$content\"")
        val value = valueMatch.value

        // Use string template for better readability
        return "${JUnitRegexp.TMS_STEP}\n${JUnitRegexp.TMS_TITLE}($value)"
    }

    private fun parseLinkMethod(content: String): String
    {
        val urlMatch = Regex(JUnitRegexp.LINK_URL_PARAMETER).find(content)
            ?: throw Exception("Can't getting url from annotation \"$content\"")
        val nameMatch = Regex(JUnitRegexp.LINK_NAME_PARAMETER).find(content)

        val url = urlMatch.groups.get(JUnitRegexp.LINK_URL_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(JUnitRegexp.LINK_NAME_PARAMETER_NAME)?.value
        val titleBlock = if (name != null) "${JUnitRegexp.PARAMETERS_SEPARATOR_OBJECT}title=${name}" else ""

        return "${JUnitRegexp.TMS_ADD_LINKS}(url=${url}${titleBlock})"
    }

    private fun parseWriteAttachMethod(content: String): String
    {
        val contentMatch = Regex(JUnitRegexp.ATTACHMENT_CONTENT_PARAMETER).find(content)
            ?: throw Exception("Can't getting content from method \"$content\"")
        val nameMatch = Regex(JUnitRegexp.ATTACHMENT_NAME_PARAMETER).find(content)

        val content = contentMatch.groups.get(JUnitRegexp.ATTACHMENT_CONTENT_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(JUnitRegexp.ATTACHMENT_NAME_PARAMETER_NAME)?.value
        val nameBlock = if (name != null) "${JUnitRegexp.PARAMETERS_SEPARATOR_OBJECT}name=${name}" else ""

        return "${JUnitRegexp.TMS_ADD_ATTACHMENTS}(\"$content\"${JUnitRegexp.PARAMETERS_SEPARATOR_OBJECT}is_text=True${nameBlock})"
    }

    private fun parseParameterMethod(content: String): String
    {
        val nameMatch = Regex(JUnitRegexp.PARAMETER_NAME_PARAMETER).find(content)
            ?: throw Exception("Can't getting name from method \"$content\"")
        val valueMatch = Regex(JUnitRegexp.PARAMETER_VALUE_PARAMETER).find(content)
            ?: throw Exception("Can't getting value from method \"$content\"")

        val name = nameMatch.groups.get(JUnitRegexp.PARAMETER_NAME_PARAMETER_NAME)?.value
        val value = valueMatch.groups.get(JUnitRegexp.PARAMETER_VALUE_PARAMETER_NAME)?.value

        // Use string template for better readability
        return "${JUnitRegexp.TMS_ADD_PARAMETER}(name=$name${JUnitRegexp.PARAMETERS_SEPARATOR_OBJECT}value=$value)"
    }

    private fun parseLabelMethod(content: String): String
    {
        val nameMatch = Regex(JUnitRegexp.PARAMETER_NAME_PARAMETER).find(content)
            ?: throw Exception("Can't getting name from method \"$content\"")
        val valueMatch = Regex(JUnitRegexp.PARAMETER_VALUE_PARAMETER).find(content)
            ?: throw Exception("Can't getting value from method \"$content\"")

        val name = nameMatch.groups.get(JUnitRegexp.PARAMETER_NAME_PARAMETER_NAME)?.value
        val value = valueMatch.groups.get(JUnitRegexp.PARAMETER_VALUE_PARAMETER_NAME)?.value

        if (name == "\"${JUnitRegexp.ALLURE_DYNAMIC_PARENT_SUITE_NAME}\"") {
            // Use string template for better readability
            return "${JUnitRegexp.TMS_ADD_NAMESPACE}($value)"
        }

        if (name == "\"${JUnitRegexp.ALLURE_DYNAMIC_SUB_SUITE_NAME}\"") {
            // Use string template for better readability
            return "${JUnitRegexp.TMS_ADD_CLASSNAME}($value)"
        }

        // Use string template for better readability
        return "${JUnitRegexp.TMS_ADD_LABELS}(${name?.dropLast(1)}:${value?.drop(1)})"
    }
}
