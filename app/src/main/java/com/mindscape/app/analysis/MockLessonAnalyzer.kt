package com.mindscape.app.analysis

import com.mindscape.app.domain.LessonBuilder
import kotlinx.coroutines.delay

/**
 * Local analyzer used until the remote AI backend and its secrets are deployed.
 *
 * It performs real URL parsing and real extractive summarization/quiz generation from a pasted
 * transcript. Without a transcript it imports a watch-only video instead of inventing content.
 */
class MockLessonAnalyzer : LessonAnalyzer {

    override val isMock: Boolean = true

    override suspend fun analyze(
        request: LessonRequest,
        onStage: (AnalysisStage) -> Unit,
    ): LessonAnalysis {
        onStage(AnalysisStage.VALIDATING)
        val link = VideoLinks.parse(request.url)
            ?: throw AnalysisException("Use a YouTube link or a direct MP4/HLS URL.")

        delay(250)
        onStage(AnalysisStage.RESOLVING)

        val transcript = request.transcript?.trim()?.takeIf { it.isNotEmpty() }
        val built = transcript?.let(LessonBuilder::build)

        delay(250)
        onStage(AnalysisStage.ANALYZING)
        delay(250)
        onStage(AnalysisStage.SAVING)

        val known = (link as? VideoLink.YouTube)?.let { knownVideos[it.id] }
        val title = known?.title ?: when (link) {
            is VideoLink.YouTube -> "YouTube lesson ${link.id}"
            is VideoLink.Direct -> link.fileName
        }
        val author = known?.author ?: if (link is VideoLink.YouTube) "@youtube" else "@direct"
        val summary = built?.summary ?: (
            "Video saved to ${request.nodeTitle}. Paste a transcript during import to generate " +
                "a summary and quiz; the mock analyzer does not invent video content."
            )

        return LessonAnalysis(
            title = title,
            author = author,
            summary = summary,
            keyPoints = built?.keyPoints.orEmpty(),
            quiz = built?.quiz,
            sourceUrl = link.url,
            videoId = (link as? VideoLink.YouTube)?.id,
            thumbnailUrl = (link as? VideoLink.YouTube)?.let {
                "https://i.ytimg.com/vi/${it.id}/hqdefault.jpg"
            },
            transcript = transcript,
        )
    }

    private data class KnownVideo(val title: String, val author: String)

    private val knownVideos = mapOf(
        "p7bzE1E5PMY" to KnownVideo(
            "Visualization of Quantum Physics (Quantum Mechanics)",
            "@udiprod",
        ),
        "ZuvK-od647c" to KnownVideo(
            "Quantum Entanglement & Spooky Action at a Distance",
            "@veritasium",
        ),
        "JhHMJCUmq28" to KnownVideo(
            "Quantum Computers Explained – Limits of Human Technology",
            "@kurzgesagt",
        ),
        "Q1YqgPAtzho" to KnownVideo(
            "DR. QUANTUM - DOUBLE SLIT EXPERIMENT",
            "@AngelArt",
        ),
    )
}
