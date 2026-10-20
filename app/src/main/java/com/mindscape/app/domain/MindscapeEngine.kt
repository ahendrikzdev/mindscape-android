package com.mindscape.app.domain

import com.mindscape.app.data.Domain
import com.mindscape.app.data.LearnTab
import com.mindscape.app.data.MindscapeUiState
import com.mindscape.app.data.NodeStatus
import com.mindscape.app.data.VideoArtifact

/**
 * Pure, Android-free state transitions for the Mindscape learning loop.
 * Keeping this logic outside Compose makes it directly unit-testable on the JVM.
 */
object MindscapeEngine {

    const val XP_PER_CORRECT_ANSWER = 100

    fun selectDomain(state: MindscapeUiState, domainId: String): MindscapeUiState {
        if (state.activeDomainId == domainId) return state
        val domain = state.domains.firstOrNull { it.id == domainId } ?: return state
        val firstNode = domain.nodes.firstOrNull() ?: return state
        return selectNode(state.copy(activeDomainId = domainId), firstNode.id)
    }

    fun selectNode(state: MindscapeUiState, nodeId: String): MindscapeUiState {
        val node = state.activeDomain.nodes.firstOrNull { it.id == nodeId } ?: return state
        val firstVideo = state.videoFeed.firstOrNull { it.nodeId == node.id }
        return state.copy(
            selectedNodeId = node.id,
            activeArtifactId = firstVideo?.id,
        )
    }

    fun selectArtifact(state: MindscapeUiState, artifactId: String): MindscapeUiState =
        if (state.videoFeed.any { it.id == artifactId }) {
            state.copy(activeArtifactId = artifactId)
        } else {
            state
        }

    fun setLearnTab(state: MindscapeUiState, tab: LearnTab): MindscapeUiState =
        state.copy(learnTab = tab)

    /**
     * Records a single quiz attempt. Correct answers award XP only the first time a video is
     * completed (the HTML prototype awarded +100 on every repeat tap).
     */
    fun answerQuestion(
        state: MindscapeUiState,
        artifactId: String,
        optionIndex: Int,
    ): MindscapeUiState {
        val artifact = state.videoFeed.firstOrNull { it.id == artifactId } ?: return state
        if (artifact.tested) return state
        val quiz = artifact.quiz ?: return state
        if (optionIndex !in quiz.options.indices) return state

        val answered = state.copy(answers = state.answers + (artifactId to optionIndex))
        if (optionIndex != quiz.correctIndex) return answered

        val updatedFeed = state.videoFeed.map { video ->
            if (video.id == artifactId) video.copy(tested = true) else video
        }
        return evaluateMastery(
            answered.copy(
                videoFeed = updatedFeed,
                userXp = state.userXp + XP_PER_CORRECT_ANSWER,
            ),
            artifact.nodeId,
        )
    }

    /** Promotes a node to mastered once enough of its videos are tested, then unlocks children. */
    fun evaluateMastery(state: MindscapeUiState, nodeId: String): MindscapeUiState {
        val testedCount = state.videoFeed.count { it.nodeId == nodeId && it.tested }
        var changed = false

        val updatedDomains = state.domains.map { domain ->
            val node = domain.nodes.firstOrNull { it.id == nodeId }
            if (node == null || node.status == NodeStatus.MASTERED || testedCount < node.requiredVideos) {
                domain
            } else {
                changed = true
                domain.copy(
                    nodes = domain.nodes.map { candidate ->
                        when {
                            candidate.id == nodeId -> candidate.copy(status = NodeStatus.MASTERED)
                            candidate.parentId == nodeId && candidate.status == NodeStatus.LOCKED ->
                                candidate.copy(status = NodeStatus.UNLOCKED)
                            else -> candidate
                        }
                    },
                )
            }
        }

        return if (changed) state.copy(domains = updatedDomains) else state
    }

    fun addVideo(state: MindscapeUiState, video: VideoArtifact): MindscapeUiState {
        val owningDomain = state.domains.firstOrNull { domain ->
            domain.nodes.any { it.id == video.nodeId }
        }
        val base = if (owningDomain != null && owningDomain.id != state.activeDomainId) {
            state.copy(activeDomainId = owningDomain.id)
        } else {
            state
        }
        return base.copy(
            videoFeed = listOf(video) + base.videoFeed,
            selectedNodeId = video.nodeId,
            activeArtifactId = video.id,
            learnTab = LearnTab.LESSON,
        )
    }

    fun removeVideo(state: MindscapeUiState, videoId: String): MindscapeUiState {
        val remaining = state.videoFeed.filterNot { it.id == videoId }
        val nextActiveId = if (state.activeArtifactId != videoId) {
            state.activeArtifactId
        } else {
            remaining.firstOrNull { it.nodeId == state.selectedNodeId }?.id
                ?: remaining.firstOrNull()?.id
        }
        return state.copy(
            videoFeed = remaining,
            activeArtifactId = nextActiveId,
            answers = state.answers - videoId,
        )
    }

    fun percentMastered(domain: Domain): Float =
        if (domain.nodes.isEmpty()) 0f
        else domain.nodes.count { it.status == NodeStatus.MASTERED }.toFloat() / domain.nodes.size

    fun quizProgress(artifact: VideoArtifact?): Float = when {
        artifact?.tested == true -> 1f
        artifact?.quiz == null -> 0f
        else -> 0.2f
    }

    fun isCorrect(artifact: VideoArtifact, optionIndex: Int): Boolean =
        artifact.quiz?.correctIndex == optionIndex
}
