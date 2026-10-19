package com.mindscape.app

import androidx.lifecycle.ViewModel
import com.mindscape.app.data.LearnTab
import com.mindscape.app.data.MindscapeRepository
import com.mindscape.app.data.MindscapeUiState
import com.mindscape.app.data.Quiz
import com.mindscape.app.data.VideoArtifact
import com.mindscape.app.domain.MindscapeEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MindscapeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        MindscapeUiState(
            domains = MindscapeRepository.domains(),
            activeDomainId = "quantum",
            selectedNodeId = "q-entanglement",
            activeArtifactId = "art-3",
            videoFeed = MindscapeRepository.videos(),
            userXp = MindscapeRepository.INITIAL_XP,
        ),
    )

    val uiState: StateFlow<MindscapeUiState> = _uiState.asStateFlow()

    fun selectDomain(domainId: String) = _uiState.update {
        MindscapeEngine.selectDomain(it, domainId)
    }

    fun selectNode(nodeId: String) = _uiState.update {
        MindscapeEngine.selectNode(it, nodeId)
    }

    fun selectArtifact(artifactId: String) = _uiState.update {
        MindscapeEngine.selectArtifact(it, artifactId)
    }

    fun setLearnTab(tab: LearnTab) = _uiState.update {
        MindscapeEngine.setLearnTab(it, tab)
    }

    fun answerQuestion(artifactId: String, optionIndex: Int) = _uiState.update {
        MindscapeEngine.answerQuestion(it, artifactId, optionIndex)
    }

    fun removeVideo(videoId: String) = _uiState.update {
        MindscapeEngine.removeVideo(it, videoId)
    }

    /**
     * Simulates the prototype's "AI extraction" pipeline, then pins the new video to a node.
     * The suspension point is driven by the caller's scope so the dialog can show progress.
     */
    suspend fun importVideo(url: String, nodeId: String) {
        require(url.isNotBlank()) { "Paste a video link first." }
        delay(IMPORT_DELAY_MS)

        val state = _uiState.value
        val node = state.domains
            .flatMap { it.nodes }
            .firstOrNull { it.id == nodeId } ?: return

        val artifact = VideoArtifact(
            id = "art-${System.currentTimeMillis()}",
            title = "Key Points: ${node.title}",
            author = "@companion_ai",
            nodeId = node.id,
            tested = false,
            thumbnailRes = MindscapeRepository.importThumbnails.random(),
            caption = "Extracted key insights and main takeaways from imported short video clip.",
            summary = "Key Lesson: Direct summary of principles covering ${node.title}.",
            quiz = Quiz(
                question = "What is the main takeaway from this clip?",
                options = listOf("Scientific principle", "Random music", "Unrelated joke"),
                correctIndex = 0,
            ),
        )

        _uiState.update { MindscapeEngine.addVideo(it, artifact) }
    }

    private companion object {
        const val IMPORT_DELAY_MS = 750L
    }
}
