package ru.testit.management.parsers

import ru.testit.management.parsers.regexps.BehaveRegexp

object BehaveParser {
    // Compile patterns lazily once
    private val patternActions: Map<Regex, Any> by lazy {
        mutableMapOf(
            Regex(BehaveRegexp.IMPORT_ALLURE_OBJECT, RegexOption.MULTILINE) to BehaveRegexp.IMPORT_TMS_OBJECT,
            Regex(BehaveRegexp.ALLURE_OTHER_FUNCTIONS_LABELS, RegexOption.MULTILINE) to ::parseOtherFunctionsLabels,
            Regex(BehaveRegexp.ALLURE_STEP, RegexOption.MULTILINE) to BehaveRegexp.TMS_STEP,
            Regex(BehaveRegexp.ALLURE_LINK, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(BehaveRegexp.ALLURE_ISSUE, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(BehaveRegexp.ALLURE_TESTCASE, RegexOption.MULTILINE) to ::parseLinkAnnotation,
            Regex(BehaveRegexp.ALLURE_PARENT_SUITE, RegexOption.MULTILINE) to BehaveRegexp.TMS_NAMESPACE,
            Regex(BehaveRegexp.ALLURE_SUITE, RegexOption.MULTILINE) to BehaveRegexp.TMS_NAMESPACE,
            Regex(BehaveRegexp.ALLURE_SUB_SUITE, RegexOption.MULTILINE) to BehaveRegexp.TMS_CLASSNAME,
            Regex(BehaveRegexp.ALLURE_EPIC, RegexOption.MULTILINE) to BehaveRegexp.TMS_NAMESPACE,
            Regex(BehaveRegexp.ALLURE_STORY, RegexOption.MULTILINE) to BehaveRegexp.TMS_CLASSNAME,
            Regex(BehaveRegexp.ALLURE_PACKAGE, RegexOption.MULTILINE) to BehaveRegexp.TMS_NAMESPACE,
            Regex(BehaveRegexp.ALLURE_TEST_CLASS, RegexOption.MULTILINE) to BehaveRegexp.TMS_CLASSNAME,
            Regex(BehaveRegexp.ALLURE_TEST_METHOD, RegexOption.MULTILINE) to BehaveRegexp.TMS_DISPLAY_NAME,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_TITLE, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_DISPLAY_NAME,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_DESCRIPTION, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_DESCRIPTION,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_DESCRIPTION_HTML, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_DESCRIPTION,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_LINK, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_ISSUE, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_TESTCASES, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_TAG, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_LABELS,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_LABEL, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_LABELS,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_ID, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_LABELS,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_EPIC, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_NAMESPACE,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_FEATURE, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_NAMESPACE,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_STORY, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_CLASSNAME,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_PARENT_SUITE, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_NAMESPACE,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_SUITE, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_NAMESPACE,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_SUB_SUITE, RegexOption.MULTILINE) to BehaveRegexp.TMS_ADD_CLASSNAME,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_ATTACHMENT_WRITE, RegexOption.MULTILINE) to ::parseWriteAttachMethod,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_ATTACHMENT_READ, RegexOption.MULTILINE) to ::parseReadAttachMethod,
            Regex(BehaveRegexp.ALLURE_DYNAMIC_PARAMETER, RegexOption.MULTILINE) to ::parseParameterMethod
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
        val valueMatch = Regex(BehaveRegexp.LABEL_VALUE_ANNOTATION).find(content)
            ?: throw Exception("Can't getting value from annotation \"$content\"")

        val name = valueMatch.groups.get(BehaveRegexp.LABEL_VALUE_ANNOTATION_NAME)?.value

        return BehaveRegexp.TMS_LABELS + name
    }

    private fun parseLinkAnnotation(content: String): String
    {
        val urlMatch = Regex(BehaveRegexp.LINK_URL_ANNOTATION).find(content)
            ?: throw Exception("Can't getting url from annotation \"$content\"")
        val nameMatch = Regex(BehaveRegexp.LINK_NAME_ANNOTATION).find(content)

        val url = urlMatch.groups.get(BehaveRegexp.LINK_URL_ANNOTATION_NAME)?.value
        val name = nameMatch?.groups?.get(BehaveRegexp.LINK_NAME_ANNOTATION_NAME)?.value
        val titleBlock = if (name != null) "${BehaveRegexp.ANNOTATION_SEPARATOR_OBJECT}\"title\":\"$name\"" else ""
        return "${BehaveRegexp.TMS_LINKS}{\"url\":\"$url\"$titleBlock}"
    }

    private fun parseLinkMethod(content: String): String
    {
        val urlMatch = Regex(BehaveRegexp.LINK_URL_PARAMETER).find(content)
            ?: throw Exception("Can't getting url from annotation \"$content\"")
        val nameMatch = Regex(BehaveRegexp.LINK_NAME_PARAMETER).find(content)

        val url = urlMatch.groups.get(BehaveRegexp.LINK_URL_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(BehaveRegexp.LINK_NAME_PARAMETER_NAME)?.value
        val titleBlock = if (name != null) "${BehaveRegexp.PARAMETERS_SEPARATOR_OBJECT}title=${name}" else ""

        return "${BehaveRegexp.TMS_ADD_LINKS}(url=${url}${titleBlock})"
    }

    private fun parseWriteAttachMethod(content: String): String
    {
        val bodyMatch = Regex(BehaveRegexp.ATTACHMENT_BODY_PARAMETER).find(content)
            ?: throw Exception("Can't getting body from method \"$content\"")
        val nameMatch = Regex(BehaveRegexp.ATTACHMENT_NAME_PARAMETER).find(content)

        val body = bodyMatch.groups.get(BehaveRegexp.ATTACHMENT_BODY_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(BehaveRegexp.ATTACHMENT_NAME_PARAMETER_NAME)?.value
        val nameBlock = if (name != null) "${BehaveRegexp.PARAMETERS_SEPARATOR_OBJECT}name=${name}" else ""

        return "${BehaveRegexp.TMS_ADD_ATTACHMENTS}(${body}${BehaveRegexp.PARAMETERS_SEPARATOR_OBJECT}is_text=True${nameBlock})"
    }

    private fun parseReadAttachMethod(content: String): String
    {
        val sourceMatch = Regex(BehaveRegexp.ATTACHMENT_SOURCE_PARAMETER).find(content)
            ?: throw Exception("Can't getting source from method \"$content\"")
        val nameMatch = Regex(BehaveRegexp.ATTACHMENT_NAME_PARAMETER).find(content)

        val source = sourceMatch.groups.get(BehaveRegexp.ATTACHMENT_SOURCE_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(BehaveRegexp.ATTACHMENT_NAME_PARAMETER_NAME)?.value
        val nameBlock = if (name != null) "${BehaveRegexp.PARAMETERS_SEPARATOR_OBJECT}name=${name}" else ""

        return "${BehaveRegexp.TMS_ADD_ATTACHMENTS}(${source}${nameBlock})"
    }

    private fun parseParameterMethod(content: String): String
    {
        val nameMatch = Regex(BehaveRegexp.PARAMETER_NAME_PARAMETER).find(content)
            ?: throw Exception("Can't getting name from method \"$content\"")
        val valueMatch = Regex(BehaveRegexp.PARAMETER_VALUE_PARAMETER).find(content)
            ?: throw Exception("Can't getting value from method \"$content\"")

        val name = nameMatch.groups.get(BehaveRegexp.PARAMETER_NAME_PARAMETER_NAME)?.value
        val value = valueMatch.groups.get(BehaveRegexp.PARAMETER_VALUE_PARAMETER_NAME)?.value

        // Use string template for better readability
        return "${BehaveRegexp.TMS_ADD_PARAMETER}(name=$name${BehaveRegexp.PARAMETERS_SEPARATOR_OBJECT}value=$value)"
    }
}