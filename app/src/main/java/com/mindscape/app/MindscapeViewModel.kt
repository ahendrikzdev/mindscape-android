package com.mindscape.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mindscape.app.analysis.AnalysisStage
import com.mindscape.app.analysis.AnalyzerProvider
import com.mindscape.app.analysis.LessonAnalyzer
import com.mindscape.app.analysis.LessonRequest
import com.mindscape.app.data.LearnTab
import com.mindscape.app.data.MindscapeRepository
import com.mindscape.app.data.MindscapeStateStore
import com.mindscape.app.data.MindscapeUiState
import com.mindscape.app.data.ThumbnailCatalog
import com.mindscape.app.data.VideoArtifact
import com.mindscape.app.domain.MindscapeEngine
import java.net.URI
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MindscapeViewModel(application: Application) : AndroidViewModel(application) {

    private val store = MindscapeStateStore(application)
    private val analyzer: LessonAnalyzer = AnalyzerProvider.create(
        baseUrl = BuildConfig.BACKEND_BASE_URL,
        apiKey = BuildConfig.BACKEND_API_KEY,
    )
    val isMockMode: Boolean = analyzer.isMock

    private val _uiState = MutableStateFlow(seedState())
    val uiState: StateFlow<MindscapeUiState> = _uiState.asStateFlow()

    private val saveQueue = Channel<MindscapeUiState>(Channel.CONFLATED)
    private var hasLocalChanges = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            for (state in saveQueue) {
                runCatching { store.save(state) }
            }
        }
        viewModelScope.launch {
            val restored = store.load()
            if (restored != null && !hasLocalChanges) {
                _uiState.value = restored
            }
        }
    }

    fun selectDomain(domainId: String) = update {
        MindscapeEngine.selectDomain(it, domainId)
    }

    fun selectNode(nodeId: String) = update {
        MindscapeEngine.selectNode(it, nodeId)
    }

    fun selectArtifact(artifactId: String) = update {
        MindscapeEngine.selectArtifact(it, artifactId)
    }

    fun setLearnTab(tab: LearnTab) = update {
        MindscapeEngine.setLearnTab(it, tab)
    }

    fun answerQuestion(artifactId: String, optionIndex: Int) = update {
        MindscapeEngine.answerQuestion(it, artifactId, optionIndex)
    }

    fun removeVideo(videoId: String) = update {
        MindscapeEngine.removeVideo(it, videoId)
    }

    /**
     * Resolves a video through the configured analyzer (remote AI backend or local mock) and
     * pins the result to a topic node. The caller's scope drives progress and cancellation.
     */
    suspend fun importVideo(
        url: String,
        nodeId: String,
        transcript: String?,
        onStage: (AnalysisStage) -> Unit,
    ) {
        require(url.isNotBlank()) { "Paste a video link first." }

        val node = _uiState.value.domains
            .flatMap { it.nodes }
            .firstOrNull { it.id == nodeId }
            ?: throw IllegalArgumentException("Choose a topic node.")

        val analysis = analyzer.analyze(
            request = LessonRequest(
                url = url,
                nodeId = nodeId,
                nodeTitle = node.title,
                transcript = transcript,
            ),
            onStage = onStage,
        )

        val artifact = VideoArtifact(
            id = "art-${UUID.randomUUID()}",
            title = analysis.title,
            author = analysis.author,
            nodeId = nodeId,
            tested = false,
            thumbnailKey = ThumbnailCatalog.keyForNode(nodeId),
            caption = "Imported from ${sourceHost(analysis.sourceUrl)}",
            summary = analysis.summary,
            quiz = analysis.quiz,
            keyPoints = analysis.keyPoints,
            sourceUrl = analysis.sourceUrl,
            videoId = analysis.videoId,
            thumbnailUrl = analysis.thumbnailUrl,
            transcript = analysis.transcript?.take(MAX_TRANSCRIPT_CHARS),
        )

        update { MindscapeEngine.addVideo(it, artifact) }
    }

    private fun update(transform: (MindscapeUiState) -> MindscapeUiState) {
        hasLocalChanges = true
        val next = transform(_uiState.value)
        _uiState.value = next
        saveQueue.trySend(next)
    }

    private fun seedState(): MindscapeUiState = MindscapeUiState(
        domains = MindscapeRepository.domains(),
        activeDomainId = "quantum",
        selectedNodeId = "q-entanglement",
        activeArtifactId = "art-3",
        videoFeed = MindscapeRepository.videos(),
        userXp = MindscapeRepository.INITIAL_XP,
    )

    private fun sourceHost(url: String): String = runCatching {
        URI(url).host?.removePrefix("www.") ?: "link"
    }.getOrDefault("link")

    private companion object {
        const val MAX_TRANSCRIPT_CHARS = 20_000
    }
}

