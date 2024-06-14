package ru.testit.management.clients

import ru.testit.client.api.ProjectSectionsApi
import ru.testit.client.api.ProjectsApi
import ru.testit.client.api.WorkItemsApi
import ru.testit.client.invoker.ApiClient
import ru.testit.client.model.SectionModel
import ru.testit.client.model.WorkItemFilterModel
import ru.testit.client.model.WorkItemModel
import ru.testit.client.model.WorkItemSelectModel
import ru.testit.management.utils.MessagesUtils
import ru.testit.management.windows.settings.TmsSettingsState
import java.util.*
import java.util.logging.Logger

object TmsClient {
    private val _logger = Logger.getLogger(TmsClient::class.java.simpleName)
    private var _projectSectionsApi: ProjectSectionsApi? = null
    private var _workItemsApi: WorkItemsApi? = null

    fun getSettingsValidationErrorMsg(projectId: String, privateToken: String, url: String): String? {
        var client: ApiClient? = null

        try {
            client = getNewApiClient(url, privateToken)
            val project = ProjectsApi(client).getProjectById(projectId)

            val errorMessage = if (project == null) {
                MessagesUtils.get("api.validation.project.null.text")
            } else {
                null
            }

            return errorMessage
        } catch (exception: Throwable) {
            return String.format(
                MessagesUtils.get("api.validation.project.error.text"),
                exception.message
            )
        } finally {
            client?.httpClient?.connectionPool?.evictAll()
        }
    }

    fun refresh() {
        _projectSectionsApi?.apiClient?.httpClient?.connectionPool?.evictAll()
        _workItemsApi?.apiClient?.httpClient?.connectionPool?.evictAll()

        val client = getNewApiClient()

        _projectSectionsApi = ProjectSectionsApi(client)
        _workItemsApi = WorkItemsApi(client)
    }

    fun getSections(): Iterable<SectionModel> {
        val sections = mutableSetOf<SectionModel>()

        try {
            sections.addAll(
                _projectSectionsApi?.getSectionsByProjectId(
                    TmsSettingsState.instance.projectId,
                    null,
                    null,
                    null,
                    null,
                    null
                ).orEmpty()
            )
        } catch (exception: Throwable) {
            _logger.severe { exception.message }
        }

        return sections
    }

    fun getWorkItemsBySectionId(sectionId: UUID?): Iterable<WorkItemModel> {
        val workItems = mutableSetOf<WorkItemModel>()

        if (sectionId == null) {
            return workItems
        }

        val filter = WorkItemFilterModel()
        filter.sectionIds = setOf(sectionId)
        filter.isDeleted = false

        val request = WorkItemSelectModel()
        request.filter = filter

        try {
            _workItemsApi?.apiV2WorkItemsSearchPost(
                null,
                null,
                null,
                null,
                null,
                request
            )?.forEach { workItem ->
                _workItemsApi
                    ?.getWorkItemById(workItem.id.toString(), null, null)
                    ?.let { workItems.add(it) }
            }
        } catch (exception: Throwable) {
            _logger.severe { exception.message }
        }

        return workItems
    }

    private fun getNewApiClient(
        url: String = TmsSettingsState.instance.url,
        token: String = TmsSettingsState.instance.privateToken
    ): ApiClient {
        val client = ApiClient()

        client.setBasePath(url)
        client.setApiKeyPrefix("PrivateToken")
        client.setApiKey(token)
        client.setVerifyingSsl(false)

        return client
    }
}
