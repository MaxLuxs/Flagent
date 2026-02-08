package flagent.route.integration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubPullRequestEvent(
    val action: String,
    @SerialName("pull_request") val pullRequest: GitHubPullRequest,
    val repository: GitHubRepository? = null
)

@Serializable
data class GitHubPullRequest(
    val head: GitHubHead,
    val number: Int = 0
)

@Serializable
data class GitHubHead(
    val ref: String
)

@Serializable
data class GitHubRepository(
    @SerialName("full_name") val fullName: String = ""
)
