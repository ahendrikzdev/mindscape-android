package com.mindscape.app.analysis

import com.mindscape.app.data.Quiz
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Client for the planned AI backend. It is selected automatically when BACKEND_BASE_URL is
 * present at build time; otherwise the app uses [MockLessonAnalyzer].
 */
class RemoteLessonAnalyzer(
    baseUrl: String,
    private val apiKey: String,
) : LessonAnalyzer {

    private val normalizedBaseUrl = baseUrl.trim().trimEnd('/')

    val isConfigured: Boolean =
        normalizedBaseUrl.startsWith("http://") || normalizedBaseUrl.startsWith("https://")

    override val isMock: Boolean = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun analyze(
        request: LessonRequest,
        onStage: (AnalysisStage) -> Unit,
    ): LessonAnalysis {
        if (!isConfigured) throw AnalysisException("The AI backend is not configured.")

        onStage(AnalysisStage.VALIDATING)
        val created = withContext(Dispatchers.IO) { createJob(request) }
        onStage(AnalysisStage.RESOLVING)

        val deadline = System.currentTimeMillis() + MAX_WAIT_MS
        while (true) {
            val job = withContext(Dispatchers.IO) { getJob(created.jobId) }
            job.stage?.let(::stageFor)?.let(onStage)

            when (job.status.lowercase()) {
                "done", "completed" -> {
                    onStage(AnalysisStage.SAVING)
                    val result = job.result
                        ?: throw AnalysisException("The backend returned no analysis.")
                    return mapResult(request, result)
                }

                "error", "failed" -> throw AnalysisException(
                    job.error ?: "The backend could not analyze this video.",
                )
            }

            if (System.currentTimeMillis() > deadline) {
                throw AnalysisException("Analysis timed out. Try a shorter video.")
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    private fun createJob(request: LessonRequest): JobCreated {
        val payload = json.encodeToString(
            CreateJobRequest(url = request.url, transcript = request.transcript),
        )
        val httpRequest = baseRequest("/v1/jobs")
            .post(payload.toRequestBody(jsonMediaType))
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw AnalysisException(httpError(response.code, body))
            return runCatching { json.decodeFromString<JobCreated>(body) }
                .getOrElse {
                    throw AnalysisException("The backend returned an invalid job response.", it)
                }
        }
    }

    private fun getJob(jobId: String): JobState {
        val httpRequest = baseRequest("/v1/jobs/$jobId").get().build()
        client.newCall(httpRequest).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw AnalysisException(httpError(response.code, body))
            return runCatching { json.decodeFromString<JobState>(body) }
                .getOrElse {
                    throw AnalysisException("The backend returned an invalid job status.", it)
                }
        }
    }

    private fun baseRequest(path: String): Request.Builder =
        Request.Builder()
            .url("$normalizedBaseUrl$path")
            .apply {
                if (apiKey.isNotBlank()) {
                    header("X-API-Key", apiKey)
                }
            }

    private fun mapResult(request: LessonRequest, result: AnalysisResult): LessonAnalysis {
        if (result.summary.isBlank()) {
            throw AnalysisException("The backend returned an empty summary.")
        }
        val source = result.source
        val quiz = result.quizzes.firstOrNull()?.let { dto ->
            if (dto.options.isEmpty() || dto.correctIndex !in dto.options.indices) {
                null
            } else {
                Quiz(
                    question = dto.question,
                    options = dto.options,
                    correctIndex = dto.correctIndex,
                    explanation = dto.explanation,
                )
            }
        }
        return LessonAnalysis(
            title = source.title.ifBlank { "AI lesson" },
            author = source.author.ifBlank { "@ai" },
            summary = result.summary,
            keyPoints = result.keyPoints,
            quiz = quiz,
            sourceUrl = source.url ?: result.sourceUrl ?: request.url,
            videoId = source.id ?: (VideoLinks.parse(request.url) as? VideoLink.YouTube)?.id,
            thumbnailUrl = source.thumbnailUrl,
            transcript = result.transcript ?: request.transcript,
        )
    }

    private fun stageFor(stage: String): AnalysisStage? = when (stage.lowercase()) {
        "queued", "resolving", "validating" -> AnalysisStage.RESOLVING
        "transcribing", "downloading" -> AnalysisStage.TRANSCRIBING
        "analyzing", "summarizing" -> AnalysisStage.ANALYZING
        "saving", "done" -> AnalysisStage.SAVING
        else -> null
    }

    private fun httpError(code: Int, body: String): String {
        val detail = body.replace('\n', ' ').take(180)
        return "Backend error $code" + if (detail.isBlank()) "" else ": $detail"
    }

    private companion object {
        const val POLL_INTERVAL_MS = 2_000L
        const val MAX_WAIT_MS = 180_000L
    }
}

@Serializable
private data class CreateJobRequest(
    val url: String,
    val transcript: String? = null,
)

@Serializable
private data class JobCreated(
    @SerialName("job_id") val jobId: String,
    val status: String = "queued",
)

@Serializable
private data class JobState(
    val status: String,
    val progress: Double = 0.0,
    val stage: String? = null,
    val error: String? = null,
    val result: AnalysisResult? = null,
)

@Serializable
private data class AnalysisResult(
    val source: AnalysisSource,
    val summary: String,
    @SerialName("key_points") val keyPoints: List<String> = emptyList(),
    val quizzes: List<QuizDto> = emptyList(),
    val transcript: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
)

@Serializable
private data class AnalysisSource(
    val kind: String = "unknown",
    val id: String? = null,
    val url: String? = null,
    val title: String = "",
    val author: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
)

@Serializable
private data class QuizDto(
    val question: String,
    val options: List<String>,
    @SerialName("correct_index") val correctIndex: Int,
    val explanation: String? = null,
)
