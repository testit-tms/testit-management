package ru.testit.management.clients

import kotlinx.serialization.Contextual
import ru.testit.kotlin.adaptersapi.apis.ProjectSectionsApi
import ru.testit.kotlin.adaptersapi.apis.ProjectsApi
import ru.testit.kotlin.adaptersapi.apis.WorkItemsApi
import ru.testit.kotlin.adaptersapi.infrastructure.ApiClient
import ru.testit.kotlin.adaptersapi.models.SectionModel
import ru.testit.kotlin.adaptersapi.models.WorkItemApiResult
import ru.testit.kotlin.adaptersapi.models.WorkItemFilterApiModel
import ru.testit.kotlin.adaptersapi.models.WorkItemSelectApiModel
import ru.testit.kotlin.adaptersapi.models.WorkItemShortApiResult
import ru.testit.management.utils.StringUtils
import ru.testit.management.windows.filters.TmsFilterState
import ru.testit.management.windows.settings.TmsSettingsState
import java.util.*
import java.util.logging.Logger


class TmsClient(url: String) {
    private val _logger = Logger.getLogger(TmsClient::class.java.simpleName)
    @Contextual
    private val projectsApi: ProjectsApi
    @Contextual
    private val workItemsApi: WorkItemsApi
    @Contextual
    private val projectSectionsApi: ProjectSectionsApi

    init {
        projectsApi = ProjectsApi(url)
        init(projectsApi)
        workItemsApi = WorkItemsApi(url)
        init(workItemsApi)
        projectSectionsApi = ProjectSectionsApi(url)
        init(projectSectionsApi)
    }

    fun init(client: ApiClient,
             token: String = TmsSettingsState.instance.privateToken ) {
        client.apiKeyPrefix["Authorization"] = "PrivateToken"
        client.apiKey["Authorization"] = token
        client.verifyingSsl = false
    }

    fun getSettingsValidationErrorMsg(projectId: String, privateToken: String): String? {
        try {
            if (projectsApi.apiKey["Authorization"].isNullOrEmpty()) {
                projectsApi.apiKey["Authorization"] = privateToken
            }
            projectsApi.adaptersProjectsIdGet(UUID.fromString(projectId))

            return null
        } catch (exception: Throwable) {
            return exception.message
        }
    }

    fun getSections(): Iterable<SectionModel> {
        val sections = mutableSetOf<SectionModel>()

        try {
            sections.addAll(
                projectSectionsApi.adaptersProjectsProjectIdSectionsGet(
                    projectId = UUID.fromString(TmsSettingsState.instance.projectId),
                )
            )
        } catch (exception: Throwable) {
            _logger.severe { exception.message }
        }

        return sections
    }

    fun getWorkItemById(id: UUID): WorkItemApiResult {
        return workItemsApi.adaptersWorkItemsIdGet(id.toString())
    }

    fun getWorkItemsBySectionId(sectionId: UUID): Iterable<WorkItemShortApiResult> {
        val nameFilter = TmsFilterState.instance.testCaseName?.trim()
        val globalIdFilter = TmsFilterState.instance.testCaseGlobalId?.let { setOf(it) }
        val automationFilter = StringUtils.textToBool(TmsFilterState.instance.isAutomation)

        val filter = WorkItemFilterApiModel(
            sectionIds = setOf(sectionId),
            isDeleted = false,
            name = nameFilter,
            globalIds = globalIdFilter,
            isAutomated = automationFilter
        )

        val request = WorkItemSelectApiModel(filter = filter)
        try {
            return workItemsApi.adaptersWorkItemsSearchPost(
                workItemSelectApiModel = request
            )
        } catch (exception: Throwable) {
            _logger.severe { exception.message }
        }

        return listOf()
    }
}
