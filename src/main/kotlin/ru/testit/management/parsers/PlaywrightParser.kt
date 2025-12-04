package ru.testit.management.parsers

import ru.testit.management.parsers.regexps.PlaywrightRegexp
import kotlin.collections.joinToString
import kotlin.text.get

object PlaywrightParser {
    // Compile patterns lazily once
    private val patternActions: Map<Regex, Any> by lazy {
        mutableMapOf(
            Regex(PlaywrightRegexp.IMPORT_ALLURE_OBJECT, RegexOption.MULTILINE) to PlaywrightRegexp.IMPORT_TMS_OBJECT,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_DISPLAY_NAME, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_DISPLAY_NAME,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_DESCRIPTION, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_DESCRIPTION,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_DESCRIPTION_HTML, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_DESCRIPTION,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_LINK, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_LINKS, RegexOption.MULTILINE) to ::parseLinksMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_ISSUE, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_TMS, RegexOption.MULTILINE) to ::parseLinkMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_TAG, RegexOption.MULTILINE) to ::parseTagMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_TAGS, RegexOption.MULTILINE) to ::parseTagMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_LABEL, RegexOption.MULTILINE) to ::parseLabelMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_LABELS, RegexOption.MULTILINE) to ::parseLabelsMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_TEST_CASE_ID, RegexOption.MULTILINE) to ::parseAnotherLabelMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_ALLURE_ID, RegexOption.MULTILINE) to ::parseAnotherLabelMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_HISTORY_ID, RegexOption.MULTILINE) to ::parseAnotherLabelMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_OWNER, RegexOption.MULTILINE) to ::parseAnotherLabelMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_SEVERITY, RegexOption.MULTILINE) to ::parseAnotherLabelMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_LAYER, RegexOption.MULTILINE) to ::parseAnotherLabelMethod,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_EPIC, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_NAMESPACE,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_FEATURE, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_NAMESPACE,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_STORY, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_CLASSNAME,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_PARENT_SUITE, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_NAMESPACE,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_SUITE, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_NAMESPACE,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_SUB_SUITE, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_CLASSNAME,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_ATTACHMENT_WRITE, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_ADD_ATTACHMENTS,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_PARAMETER, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_ADD_PARAMETER,
            Regex(PlaywrightRegexp.ALLURE_DYNAMIC_STEP, RegexOption.MULTILINE) to PlaywrightRegexp.TMS_STEP,
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

    private fun parseLinkMethod(code: String): String
    {
        val urlMatch = Regex(PlaywrightRegexp.LINK_URL_PARAMETER).find(code)
            ?: throw Exception("Can't getting url from annotation \"$code\"")
        val nameMatch = Regex(PlaywrightRegexp.LINK_NAME_PARAMETER).find(code)

        val url = urlMatch.groups.get(PlaywrightRegexp.LINK_URL_PARAMETER_NAME)?.value
        val name = nameMatch?.groups?.get(PlaywrightRegexp.LINK_NAME_PARAMETER_NAME)?.value
        val titleBlock = if (name != null) "${PlaywrightRegexp.PARAMETERS_SEPARATOR_OBJECT}title: $name" else ""

        return "${PlaywrightRegexp.TMS_LINKS}([{url: ${url}${titleBlock}}])"
    }

    private fun parseLinksMethod(code: String): String
    {
        val linkMatches = Regex(PlaywrightRegexp.EVERYTHING_IN_BRACES).findAll(code)

        if (linkMatches.count() == 0) {
            throw Exception("Can't getting link from annotation \"$code\"")
        }

        val links = mutableListOf<String>()

        for (linkMatch in linkMatches) {
            val urlMatch = Regex(PlaywrightRegexp.LINK_IN_LINKS_URL_PARAMETER).find(linkMatch.value)
                ?: throw Exception("Can't getting url from annotation \"${linkMatch.value}\"")
            val nameMatch = Regex(PlaywrightRegexp.LINK_IN_LINKS_NAME_PARAMETER).find(linkMatch.value)

            val url = urlMatch.groups.get(PlaywrightRegexp.LINK_URL_PARAMETER_NAME)?.value
            val name = nameMatch?.groups?.get(PlaywrightRegexp.LINK_NAME_PARAMETER_NAME)?.value
            val titleBlock = if (name != null) "${PlaywrightRegexp.PARAMETERS_SEPARATOR_OBJECT}title: $name" else ""

            links.add("{url: ${url}${titleBlock}}")
        }

        return "${PlaywrightRegexp.TMS_LINKS}([" + links.joinToString(",\n") + "])"
    }

    private fun parseTagMethod(code: String): String
    {
        val parametersMatch = Regex(PlaywrightRegexp.EVERYTHING_IN_PARENTHESES).find(code)
            ?: throw Exception("Can't getting parameters from annotation \"$code\"")
        val tagMatches = Regex(PlaywrightRegexp.VARIABLE + "|" + PlaywrightRegexp.VALUE).findAll(parametersMatch.value)

        if (tagMatches.count() == 0) {
            throw Exception("Can't getting tag from annotation \"$code\"")
        }

        val tags = mutableListOf<String>()

        tagMatches.forEach { tagMatch -> tags.add(tagMatch.value) }

        // Use string template for better readability
        return "${PlaywrightRegexp.TMS_LABELS}([" + tags.joinToString(",\n") + "])"
    }

    private fun parseLabelMethod(code: String): String
    {
        val nameMatch = Regex(PlaywrightRegexp.LABEL_NAME_PARAMETER).find(code)
            ?: throw Exception("Can't getting name from method \"$code\"")
        val valueMatch = Regex(PlaywrightRegexp.LABEL_VALUE_PARAMETER).find(code)
            ?: throw Exception("Can't getting value from method \"$code\"")

        val name = nameMatch.groups.get(PlaywrightRegexp.LABEL_NAME_PARAMETER_NAME)?.value
        val value = valueMatch.groups.get(PlaywrightRegexp.LABEL_VALUE_PARAMETER_NAME)?.value

        // Use string template for better readability
        return "${PlaywrightRegexp.TMS_LABELS}([{\"$name:$value\"}])"
    }

    private fun parseLabelsMethod(code: String): String
    {
        val labelMatches = Regex(PlaywrightRegexp.EVERYTHING_IN_BRACES).findAll(code)

        if (labelMatches.count() == 0) {
            throw Exception("Can't getting label from annotation \"$code\"")
        }

        val labels = mutableListOf<String>()

        for (labelMatch in labelMatches) {
            val nameMatch = Regex(PlaywrightRegexp.LABEL_NAME_PARAMETER).find(labelMatch.value)
                ?: throw Exception("Can't getting name from method \"${labelMatch.value}\"")
            val valueMatch = Regex(PlaywrightRegexp.LABEL_VALUE_PARAMETER).find(labelMatch.value)
                ?: throw Exception("Can't getting value from method \"${labelMatch.value}\"")

            val name = nameMatch.groups.get(PlaywrightRegexp.LABEL_NAME_PARAMETER_NAME)?.value
            val value = valueMatch.groups.get(PlaywrightRegexp.LABEL_VALUE_PARAMETER_NAME)?.value

            labels.add("{\"$name:$value\"}")
        }

        // Use string template for better readability
        return "${PlaywrightRegexp.TMS_LABELS}([" + labels.joinToString(",\n") + "])"
    }

    private fun parseAnotherLabelMethod(code: String): String
    {
        val nameMatch = Regex(PlaywrightRegexp.ANNOTATION_SEPARATOR + PlaywrightRegexp.VARIABLE + "\\(").find(code)
            ?: throw Exception("Can't getting name from annotation \"$code\"")
        val valueMatch = Regex("\\(" + PlaywrightRegexp.VARIABLE + "|" + PlaywrightRegexp.VALUE + "\\)").find(code)
            ?: throw Exception("Can't getting value from annotation \"$code\"")

        val name = nameMatch.value.drop(1).dropLast(1)
        val value = valueMatch.value.drop(1).dropLast(1).replace("\"", "")

        return "${PlaywrightRegexp.TMS_LABELS}([{\"$name:$value\"}])"
    }
}
