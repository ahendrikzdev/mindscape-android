package com.mindscape.app.analysis

import com.mindscape.app.data.Quiz

enum class AnalysisStage { VALIDATING, RESOLVING, TRANSCRIBING, ANALYZING, SAVING }

data class LessonRequest(
    val url: String,
    val nodeId: String,
    val nodeTitle: String,
    val transcript: String?,
)

data class LessonAnalysis(
    val title: String,
    val author: String,
    val summary: String,
    val keyPoints: List<String>,
    val quiz: Quiz?,
    val sourceUrl: String,
    val videoId: String?,
    val thumbnailUrl: String?,
    val transcript: String?,
)

class AnalysisException(message: String, cause: Throwable? = null) : Exception(message, cause)

interface LessonAnalyzer {
    val isMock: Boolean
    suspend fun analyze(request: LessonRequest, onStage: (AnalysisStage) -> Unit): LessonAnalysis
}
