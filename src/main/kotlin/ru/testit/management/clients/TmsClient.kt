package ru.testit.management.clients

import kotlinx.serialization.Contextual
import ru.testit.kotlin.client.apis.*
import ru.testit.kotlin.client.infrastructure.ApiClient
import ru.testit.kotlin.client.models.*
import ru.testit.management.windows.settings.TmsSettingsState
import java.util.*
import java.util.logging.Logger


class TmsClient(url: String) {
    private val _logger = Logger.getLogger(TmsClient::class.java.simpleName)
    @Contextual
    private val testRunsApi: TestRunsApi
    @Contextual
    private val autoTestsApi: AutoTestsApi
    @Contextual
    private val attachmentsApi: AttachmentsApi
    @Contextual
    private val testResultsApi: TestResultsApi
    @Contextual
    private val projectsApi: ProjectsApi
    @Contextual
    private val workItemsApi: WorkItemsApi
    @Contextual
    private val projectSectionsApi: ProjectSectionsApi



    init {
        testRunsApi = TestRunsApi(url)
        init(testRunsApi)
        autoTestsApi = AutoTestsApi(url)
        init(autoTestsApi)
        attachmentsApi = AttachmentsApi(url)
        init(attachmentsApi)
        testResultsApi = TestResultsApi(url)
        init(testResultsApi)
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
            projectsApi.getProjectById(projectId)

            return null
        } catch (exception: Throwable) {
            return exception.message
        }
    }

    fun getSections(): Iterable<SectionModel> {
        val sections = mutableSetOf<SectionModel>()

        try {
            sections.addAll(
                projectSectionsApi.getSectionsByProjectId(
                    projectId = TmsSettingsState.instance.projectId,
                )
            )
        } catch (exception: Throwable) {
            _logger.severe { exception.message }
        }

        return sections
    }

    fun getWorkItemById(id: UUID): WorkItemModel {
        return workItemsApi.getWorkItemById(id.toString(), null, null)
    }

    fun getWorkItemsBySectionId(sectionId: UUID?): Iterable<WorkItemShortApiResult> {

        if (sectionId == null) {
            return listOf()
        }

        val filter = WorkItemFilterApiModel(sectionIds = setOf(sectionId), isDeleted = false)
        val request = WorkItemSelectApiModel(filter = filter)
        try {
            val workItemsList = workItemsApi.apiV2WorkItemsSearchPost(
                workItemSelectApiModel = request
            )
            return workItemsList
        } catch (exception: Throwable) {
            _logger.severe { exception.message }
        }

        return listOf()
    }
}
