package flagent.api.constants

/**
 * API constants shared across backend and frontend.
 */
object ApiConstants {
    const val API_VERSION = "v1"
    const val API_BASE_PATH = "/api/$API_VERSION"

    object Paths {
        const val FLAGS = "/flags"
        const val EVALUATION = "/evaluation"
        const val SEGMENTS = "/segments"
        const val VARIANTS = "/variants"
        const val CONSTRAINTS = "/constraints"
        const val DISTRIBUTIONS = "/distributions"
        const val TAGS = "/tags"
        const val EXPORT = "/export"
        const val HEALTH = "/health"
        const val INFO = "/info"
    }

    object Headers {
        const val TENANT_ID = "X-Tenant-ID"
        const val API_KEY = "X-API-Key"
        const val CONTENT_TYPE = "Content-Type"
        const val AUTHORIZATION = "Authorization"
    }

    object ContentTypes {
        const val JSON = "application/json"
        const val YAML = "application/x-yaml"
    }

    object QueryParams {
        const val PAGE = "page"
        const val PAGE_SIZE = "pageSize"
        const val SORT_BY = "sortBy"
        const val ORDER = "order"
        const val FILTER = "filter"
    }
}
