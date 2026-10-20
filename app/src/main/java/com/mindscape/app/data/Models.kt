package com.mindscape.app.data

import kotlinx.serialization.Serializable

@Serializable
enum class NodeStatus { MASTERED, UNLOCKED, LOCKED }

@Serializable
enum class LearnTab { LESSON, QUIZ }

@Serializable
enum class NodeIcon { ATOM, LINK, MICROCHIP, KEY, BOLT, SCROLL, MONUMENT, LANGUAGE }

@Serializable
data class Quiz(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null,
)

@Serializable
data class VideoArtifact(
    val id: String,
    val title: String,
    val author: String,
    val nodeId: String,
    val tested: Boolean,
    val thumbnailKey: String,
    val caption: String,
    val summary: String,
    val quiz: Quiz? = null,
    val keyPoints: List<String> = emptyList(),
    val sourceUrl: String? = null,
    val videoId: String? = null,
    val thumbnailUrl: String? = null,
    val transcript: String? = null,
    val watched: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

@Serializable
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

@Serializable
data class Domain(
    val id: String,
    val label: String,
    val nodes: List<SkillNode>,
)

@Serializable
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
