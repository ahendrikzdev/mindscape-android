package com.mindscape.app.data

enum class NodeStatus { MASTERED, UNLOCKED, LOCKED }

enum class LearnTab { LESSON, QUIZ }

enum class NodeIcon { ATOM, LINK, MICROCHIP, KEY, BOLT, SCROLL, MONUMENT, LANGUAGE }

data class Quiz(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
)

data class VideoArtifact(
    val id: String,
    val title: String,
    val author: String,
    val nodeId: String,
    val tested: Boolean,
    val thumbnailRes: Int,
    val caption: String,
    val summary: String,
    val quiz: Quiz,
)

data class SkillNode(
    val id: String,
    val title: String,
    val status: NodeStatus,
    val parentId: String?,
    val xPercent: Float,
    val yPercent: Float,
    val icon: NodeIcon,
    val xp: Int,
    val requiredVideos: Int,
)

data class Domain(
    val id: String,
    val label: String,
    val nodes: List<SkillNode>,
)

data class MindscapeUiState(
    val domains: List<Domain>,
    val activeDomainId: String,
    val selectedNodeId: String,
    val activeArtifactId: String?,
    val videoFeed: List<VideoArtifact>,
    val userXp: Int,
    val answers: Map<String, Int> = emptyMap(),
    val learnTab: LearnTab = LearnTab.LESSON,
) {
    val activeDomain: Domain
        get() = domains.firstOrNull { it.id == activeDomainId } ?: domains.first()

    val selectedNode: SkillNode?
        get() = activeDomain.nodes.firstOrNull { it.id == selectedNodeId }

    val activeArtifact: VideoArtifact?
        get() = videoFeed.firstOrNull { it.id == activeArtifactId }

    fun videosForNode(nodeId: String): List<VideoArtifact> =
        videoFeed.filter { it.nodeId == nodeId }
}
